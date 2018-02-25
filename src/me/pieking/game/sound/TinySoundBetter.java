/*
 * Copyright (c) 2012, Finn Kuusisto
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package me.pieking.game.sound;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.internal.MemMusic;
import kuusisto.tinysound.internal.MemSound;
import kuusisto.tinysound.internal.Mixer;
import kuusisto.tinysound.internal.StreamInfo;
import kuusisto.tinysound.internal.StreamMusic;
import kuusisto.tinysound.internal.StreamSound;
import kuusisto.tinysound.internal.UpdateRunner;

/**
 * TinySoundBetter is the main class of the TinySoundBetter system.  In order to use the
 * TinySoundBetter system, it must be initialized.  After that, Music and Sound
 * objects can be loaded and used.  When finished with the TinySoundBetter system, it
 * must be shutdown.
 * 
 * @author Finn Kuusisto
 */
public class TinySoundBetter {
	
	public static final String VERSION = "1.1.1";

	/**
	 * The internal format used by TinySoundBetter.
	 */
	public static final AudioFormat FORMAT = new AudioFormat(
			AudioFormat.Encoding.PCM_SIGNED, //linear signed PCM
			44100, //44.1kHz sampling rate
			16, //16-bit
			2, //2 channels fool
			4, //frame size 4 bytes (16-bit, 2 channel)
			44100, //same as sampling rate
			false //little-endian
			);
	
	//the system has only one mixer for both music and sounds
	private static Mixer mixer;
	//need a line to the speakers
	private static SourceDataLine outLine;
	//see if the system has been initialized
	private static boolean inited = false;
	//auto-updater for the system
	private static UpdateRunner autoUpdater;
	//counter for unique sound IDs
	private static int soundCount = 0;
	
	/**
	 * Initialize Tinysound.  This must be called before loading audio.
	 */
	public static void init() {
		if (TinySoundBetter.inited) {
			return;
		}
		//try to open a line to the speakers
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				TinySoundBetter.FORMAT);
		if (!AudioSystem.isLineSupported(info)) {
		    System.err.println("Unsupported output format!");
		    return;
		}
		TinySoundBetter.outLine = TinySoundBetter.tryGetLine();
		if (TinySoundBetter.outLine == null) {
		    System.err.println("Output line unavailable!");
		    return;
		}
		//start the line and finish initialization
		TinySoundBetter.outLine.start();
		TinySoundBetter.finishInit();
	}
	
	/**
	 * Alternative function to initialize TinySoundBetter which should only be used by
	 * those very familiar with the Java Sound API.  This function allows the
	 * line that is used for audio playback to be opened on a specific Mixer.
	 * @param info the Mixer.Info representing the desired Mixer
	 * @throws LineUnavailableException if a Line is not available from the
	 * specified Mixer
	 * @throws SecurityException if the specified Mixer or Line are unavailable
	 * due to security restrictions
	 * @throws IllegalArgumentException if the specified Mixer is not installed
	 * on the system
	 */
	public static void init(javax.sound.sampled.Mixer.Info info) 
			throws LineUnavailableException, SecurityException,
			IllegalArgumentException {
		if (TinySoundBetter.inited) {
			return;
		}
		//try to open a line to the speakers
		javax.sound.sampled.Mixer mixer = AudioSystem.getMixer(info);
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
				TinySoundBetter.FORMAT);
		TinySoundBetter.outLine = (SourceDataLine)mixer.getLine(lineInfo);
		TinySoundBetter.outLine.open(TinySoundBetter.FORMAT);
		//start the line and finish initialization
		TinySoundBetter.outLine.start();
		TinySoundBetter.finishInit();
	}
	
	/**
	 * Initializes the mixer and updater, and marks TinySoundBetter as initialized.
	 */
	private static void finishInit() {
		//now initialize the mixer
		TinySoundBetter.mixer = new Mixer();
		//initialize and start the updater
		TinySoundBetter.autoUpdater = new UpdateRunner(TinySoundBetter.mixer,
				TinySoundBetter.outLine);
		Thread updateThread = new Thread(TinySoundBetter.autoUpdater);
		try {
			updateThread.setDaemon(true);
			updateThread.setPriority(Thread.MAX_PRIORITY);
		} catch (Exception e) {}
		TinySoundBetter.inited = true;
		updateThread.start();
		//yield to potentially give the updater a chance
		Thread.yield();
	}
	
	/**
	 * Shutdown TinySoundBetter.
	 */
	public static void shutdown() {
		if (!TinySoundBetter.inited) {
			return;
		}
		TinySoundBetter.inited = false;
		//stop the auto-updater if running
		TinySoundBetter.autoUpdater.stop();
		TinySoundBetter.autoUpdater = null;
		TinySoundBetter.outLine.stop();
		TinySoundBetter.outLine.flush();
		TinySoundBetter.mixer.clearMusic();
		TinySoundBetter.mixer.clearSounds();
		TinySoundBetter.mixer = null;
	}
	
	/**
	 * Determine if TinySoundBetter is initialized and ready for use.
	 * @return true if TinySoundBetter is initialized, false if TinySoundBetter has not been
	 * initialized or has subsequently been shutdown
	 */
	public static boolean isInitialized() {
		return TinySoundBetter.inited;
	}
	
	/**
	 * Get the global volume for all audio.
	 * @return the global volume for all audio, -1.0 if TinySoundBetter has not been
	 * initialized or has subsequently been shutdown
	 */
	public static double getGlobalVolume() {
		if (!TinySoundBetter.inited) {
			return -1.0;
		}
		return TinySoundBetter.mixer.getVolume();
	}
	
	/**
	 * Set the global volume.  This is an extra multiplier, not a replacement,
	 * for all Music and Sound volume settings.  It starts at 1.0.
	 * @param volume the global volume to set
	 */
	public static void setGlobalVolume(double volume) {
		if (!TinySoundBetter.inited) {
			return;
		}
		TinySoundBetter.mixer.setVolume(volume);
	}
	
	/**
	 * Load a Music by a resource name.  The resource must be on the classpath
	 * for this to work.  This will store audio data in memory.
	 * @param name name of the Music resource
	 * @return Music resource as specified, null if not found/loaded
	 */
	public static Music loadMusic(String name) {
		return TinySoundBetter.loadMusic(name, false);
	}
	
	/**
	 * Load a Music by a resource name.  The resource must be on the classpath
	 * for this to work.
	 * @param name name of the Music resource
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music resource as specified, null if not found/loaded
	 */
	public static Music loadMusic(String name, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (name == null) {
			return null;
		}
		//check for correct naming
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		URL url = TinySoundBetter.class.getResource(name);
		//check for failure to find resource
		if (url == null) {
			System.err.println("Unable to find resource " + name + "!");
			return null;
		}
		return TinySoundBetter.loadMusic(url, streamFromFile);
	}
	
	/**
	 * Load a Music by a File.  This will store audio data in memory.
	 * @param file the Music file to load
	 * @return Music from file as specified, null if not found/loaded
	 */
	public static Music loadMusic(File file) {
		return TinySoundBetter.loadMusic(file, false);
	}
	
	/**
	 * Load a Music by a File.
	 * @param file the Music file to load
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music from file as specified, null if not found/loaded
	 */
	public static Music loadMusic(File file, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (file == null) {
			return null;
		}
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			System.err.println("Unable to find file " + file + "!");
			return null;
		}
		return TinySoundBetter.loadMusic(url, streamFromFile);
	}
	
	/**
	 * Load a Music by a URL.  This will store audio data in memory.
	 * @param url the URL of the Music
	 * @return Music from URL as specified, null if not found/loaded
	 */
	public static Music loadMusic(URL url) {
		return TinySoundBetter.loadMusic(url, false);
	}
	
	/**
	 * Load a Music by a URL.
	 * @param url the URL of the Music
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Music from URL as specified, null if not found/loaded
	 */
	public static Music loadMusic(URL url, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (url == null) {
			return null;
		}
		//get a valid stream of audio data
		AudioInputStream audioStream = TinySoundBetter.getValidAudioStream(url);
		//check for failure
		if (audioStream == null) {
			return null;
		}
		//try to read all the bytes
		byte[][] data = TinySoundBetter.readAllBytes(audioStream);
		//check for failure
		if (data == null) {
			return null;
		}
		//handle differently if streaming from a file
		if (streamFromFile) {
			StreamInfo info = TinySoundBetter.createFileStream(data);
			//check for failure
			if (info == null) {
				return null;
			}
			//try to create it
			StreamMusic sm = null;
			try {
				sm = new StreamMusic(info.URL, info.NUM_BYTES_PER_CHANNEL,
						TinySoundBetter.mixer);
			} catch (IOException e) {
				System.err.println("Failed to create StreamMusic!");
			}
			return sm;
		}
		//construct the Music object and register it with the mixer
		return new MemMusic(data[0], data[1], TinySoundBetter.mixer);
	}
	
	/**
	 * Load a Sound by a resource name.  The resource must be on the classpath
	 * for this to work.  This will store audio data in memory.
	 * @param name name of the Sound resource
	 * @return Sound resource as specified, null if not found/loaded
	 */
	public static Sound loadSound(String name) {
		return TinySoundBetter.loadSound(name, false);
	}
	
	/**
	 * Load a Sound by a resource name.  The resource must be on the classpath
	 * for this to work.
	 * @param name name of the Sound resource
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Sound resource as specified, null if not found/loaded
	 */
	public static Sound loadSound(String name, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (name == null) {
			return null;
		}
		//check for correct naming
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		URL url = TinySoundBetter.class.getResource(name);
		//check for failure to find resource
		if (url == null) {
			System.err.println("Unable to find resource " + name + "!");
			return null;
		}
		return TinySoundBetter.loadSound(url, streamFromFile);

	}
	
	/**
	 * Load a Sound by a File.  This will store audio data in memory.
	 * @param file the Sound file to load
	 * @return Sound from file as specified, null if not found/loaded
	 */
	public static Sound loadSound(File file) {
		return TinySoundBetter.loadSound(file, false);
	}
	
	/**
	 * Load a Sound by a File.
	 * @param file the Sound file to load
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Sound from file as specified, null if not found/loaded
	 */
	public static Sound loadSound(File file, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (file == null) {
			return null;
		}
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (MalformedURLException e) {
			System.err.println("Unable to find file " + file + "!");
			return null;
		}
		return TinySoundBetter.loadSound(url, streamFromFile);
	}
	
	/**
	 * Load a Sound by a URL.  This will store audio data in memory.
	 * @param url the URL of the Sound
	 * @return Sound from URL as specified, null if not found/loaded
	 */
	public static Sound loadSound(URL url) {
		return TinySoundBetter.loadSound(url, false);
	}
	
	/**
	 * Load a Sound by a URL.  This will store audio data in memory.
	 * @param url the URL of the Sound
	 * @param streamFromFile true if this Music should be streamed from a
	 * temporary file to reduce memory overhead
	 * @return Sound from URL as specified, null if not found/loaded
	 */
	public static Sound loadSound(URL url, boolean streamFromFile) {
		//check if the system is initialized
		if (!TinySoundBetter.inited) {
			System.err.println("TinySoundBetter not initialized!");
			return null;
		}
		//check for failure
		if (url == null) {
			return null;
		}
		//get a valid stream of audio data
		AudioInputStream audioStream = TinySoundBetter.getValidAudioStream(url);
		//check for failure
		if (audioStream == null) {
			return null;
		}
		//try to read all the bytes
		byte[][] data = TinySoundBetter.readAllBytes(audioStream);
		//check for failure
		if (data == null) {
			return null;
		}
		//handle differently if streaming from file
		if (streamFromFile) {
			StreamInfo info = TinySoundBetter.createFileStream(data);
			//check for failure
			if (info == null) {
				return null;
			}
			//try to create it
			StreamSound ss = null;
			try {
				ss = new StreamSound(info.URL, info.NUM_BYTES_PER_CHANNEL,
						TinySoundBetter.mixer, TinySoundBetter.soundCount);
				TinySoundBetter.soundCount++;
			} catch (IOException e) {
				System.err.println("Failed to create StreamSound!");
			}
			return ss;
		}
		//construct the Sound object
		TinySoundBetter.soundCount++;
		return new MemSound(data[0], data[1], TinySoundBetter.mixer,
				TinySoundBetter.soundCount);
	}
	
	/**
	 * Reads all of the bytes from an AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[][] readAllBytes(AudioInputStream stream) {
		//left and right channels
		byte[][] data = null;
		int numChannels = stream.getFormat().getChannels();
		//handle 1-channel
		if (numChannels == 1) {
			byte[] left = TinySoundBetter.readAllBytesOneChannel(stream);
			//check failure
			if (left == null) {
				return null;
			}
			data = new byte[2][];
			data[0] = left;
			data[1] = left; //don't copy for the right channel
		} //handle 2-channel
		else if (numChannels == 2) {
			data = TinySoundBetter.readAllBytesTwoChannel(stream);
		}
		else { //wtf?
			System.err.println("Unable to read " + numChannels + " channels!");
		}
		return data;
	}
	
	/**
	 * Reads all of the bytes from a 1-channel AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[] readAllBytesOneChannel(AudioInputStream stream) {
		//read all the bytes (assuming 1-channel)
		byte[] data = null;
		try {
			data = TinySoundBetter.getBytes(stream);
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		return data;
	}
	
	/**
	 * Reads all of the bytes from a 2-channel AudioInputStream.
	 * @param stream the stream to read
	 * @return all bytes from the stream, null if error
	 */
	private static byte[][] readAllBytesTwoChannel(AudioInputStream stream) {
		//read all the bytes (assuming 16-bit, 2-channel)
		byte[][] data = null;
		try {
			byte[] allBytes = TinySoundBetter.getBytes(stream);
			
			System.out.println(allBytes.length);
			
			byte[] left = new byte[allBytes.length / 2];
			byte[] right = new byte[allBytes.length / 2];
			for (int i = 0, j = 0; i < allBytes.length; i += 4, j += 2) {
				//interleaved left then right
				left[j] = allBytes[i];
				left[j + 1] = allBytes[i + 1];
				right[j] = allBytes[i + 2];
				right[j + 1] = allBytes[i + 3];
			}
			data = new byte[2][];
			data[0] = left;
			data[1] = right;
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		return data;
	}
	
	/**
	 * Gets and AudioInputStream in the TinySoundBetter system format.
	 * @param url URL of the resource
	 * @return the specified stream as an AudioInputStream stream, null if
	 * failure
	 */
	private static AudioInputStream getValidAudioStream(URL url) {
		AudioInputStream audioStream = null;
		try {
			audioStream = AudioSystem.getAudioInputStream(url);
			AudioFormat streamFormat = audioStream.getFormat();
			//1-channel can also be treated as stereo
			AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 16, 1, 2, 44100, false);
			//1 or 2 channel 8-bit may be easy to convert
			AudioFormat mono8 =	new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					44100, 8, 1, 1, 44100, false);
			AudioFormat stereo8 =
				new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 2, 2,
					44100, false);
			//now check formats (attempt conversion as needed)
			if (streamFormat.matches(TinySoundBetter.FORMAT) ||
					streamFormat.matches(mono16)) {
				return audioStream;
			} //check conversion to TinySoundBetter format
			else if (AudioSystem.isConversionSupported(TinySoundBetter.FORMAT,
					streamFormat)) {
				audioStream = AudioSystem.getAudioInputStream(TinySoundBetter.FORMAT,
						audioStream);
			} //check conversion to mono alternate
			else if (AudioSystem.isConversionSupported(mono16, streamFormat)) {
				audioStream = AudioSystem.getAudioInputStream(mono16,
						audioStream);
			} //try convert from 8-bit, 2-channel
			else if (streamFormat.matches(stereo8) ||
					AudioSystem.isConversionSupported(stereo8, streamFormat)) {
				//convert to 8-bit stereo first?
				if (!streamFormat.matches(stereo8)) {
					audioStream = AudioSystem.getAudioInputStream(stereo8,
							audioStream);
				}
				audioStream = TinySoundBetter.convertStereo8Bit(audioStream);
			} //try convert from 8-bit, 1-channel
			else if (streamFormat.matches(mono8) ||
					AudioSystem.isConversionSupported(mono8, streamFormat)) {
				//convert to 8-bit mono first?
				if (!streamFormat.matches(mono8)) {
					audioStream = AudioSystem.getAudioInputStream(mono8,
							audioStream);
				}
				audioStream = TinySoundBetter.convertMono8Bit(audioStream);
			} //it's time to give up
			else {
				System.err.println("Unable to convert audio resource!");
				System.err.println(url);
				System.err.println(streamFormat);
				audioStream.close();
				return null;
			}
			//check the frame length
			long frameLength = audioStream.getFrameLength();
			//too long
			if (frameLength > Integer.MAX_VALUE) {
				System.err.println("Audio resource too long!");
				return null;
			}
		}
		catch (UnsupportedAudioFileException e) {
			System.err.println("Unsupported audio resource!\n" +
					e.getMessage());
			return null;
		}
		catch (IOException e) {
			System.err.println("Error getting resource stream!\n" +
					e.getMessage());
			return null;
		}
		return audioStream;
	}
	
	/**
	 * Converts an 8-bit, signed, 1-channel AudioInputStream to 16-bit, signed,
	 * 1-channel.
	 * @param stream stream to convert
	 * @return converted stream
	 */
	private static AudioInputStream convertMono8Bit(AudioInputStream stream) {
		//assuming 8-bit, 1-channel to 16-bit, 1-channel
		byte[] newData = null;
		try {
			byte[] data = TinySoundBetter.getBytes(stream);
			int newNumBytes = data.length * 2;
			//check if size overflowed
			if (newNumBytes < 0) {
				System.err.println("Audio resource too long!");
				return null;
			}
			newData = new byte[newNumBytes];
			//convert bytes one-by-one to int, and then to 16-bit
			for (int i = 0, j = 0; i < data.length; i++, j += 2) {
				//convert it to a double
				double floatVal = (double)data[i];
				floatVal /= (floatVal < 0) ? 128 : 127;
				if (floatVal < -1.0) { //just in case
					floatVal = -1.0;
				}
				else if (floatVal > 1.0) {
					floatVal = 1.0;
				}
				//convert it to an int and then to 2 bytes
				int val = (int)(floatVal * Short.MAX_VALUE);
				newData[j + 1] = (byte)((val >> 8) & 0xFF); //MSB
				newData[j] = (byte)(val & 0xFF); //LSB
			}
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		AudioFormat mono16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100, 16, 1, 2, 44100, false);
		return new AudioInputStream(new ByteArrayInputStream(newData), mono16,
				newData.length / 2);
	}
	
	/**
	 * Converts an 8-bit, signed, 2-channel AudioInputStream to 16-bit, signed,
	 * 2-channel.
	 * @param stream stream to convert
	 * @return converted stream
	 */
	private static AudioInputStream convertStereo8Bit(AudioInputStream stream) {
		//assuming 8-bit, 2-channel to 16-bit, 2-channel
		byte[] newData = null;
		try {
			byte[] data = TinySoundBetter.getBytes(stream);
			int newNumBytes = data.length * 2 * 2;
			//check if size overflowed
			if (newNumBytes < 0) {
				System.err.println("Audio resource too long!");
				return null;
			}
			newData = new byte[newNumBytes];
			for (int i = 0, j = 0; i < data.length; i += 2, j += 4) {
				//convert them to doubles
				double leftFloatVal = (double)data[i];
				double rightFloatVal = (double)data[i + 1];
				leftFloatVal /= (leftFloatVal < 0) ? 128 : 127;
				rightFloatVal /= (rightFloatVal < 0) ? 128 : 127;
				if (leftFloatVal < -1.0) { //just in case
					leftFloatVal = -1.0;
				}
				else if (leftFloatVal > 1.0) {
					leftFloatVal = 1.0;
				}
				if (rightFloatVal < -1.0) { //just in case
					rightFloatVal = -1.0;
				}
				else if (rightFloatVal > 1.0) {
					rightFloatVal = 1.0;
				}
				//convert them to ints and then to 2 bytes each
				int leftVal = (int)(leftFloatVal * Short.MAX_VALUE);
				int rightVal = (int)(rightFloatVal * Short.MAX_VALUE);
				//left channel bytes
				newData[j + 1] = (byte)((leftVal >> 8) & 0xFF); //MSB
				newData[j] = (byte)(leftVal & 0xFF); //LSB
				//then right channel bytes
				newData[j + 3] = (byte)((rightVal >> 8) & 0xFF); //MSB
				newData[j + 2] = (byte)(rightVal & 0xFF); //LSB
			}
		}
		catch (IOException e) {
			System.err.println("Error reading all bytes from stream!");
			return null;
		}
		finally {
			try { stream.close(); } catch (IOException e) {}
		}
		AudioFormat stereo16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				44100, 16, 2, 4, 44100, false);
		return new AudioInputStream(new ByteArrayInputStream(newData), stereo16,
				newData.length / 4);
	}
	
	/**
	 * Read all of the bytes from an AudioInputStream.
	 * @param stream the stream from which to read bytes
	 * @return all bytes read from the AudioInputStream
	 * @throws IOException 
	 */
	private static byte[] getBytes(AudioInputStream stream)
			throws IOException {
		//buffer 1-sec at a time
//		int bufSize = (int)TinySoundBetter.FORMAT.getSampleRate() *
//			TinySoundBetter.FORMAT.getChannels() * TinySoundBetter.FORMAT.getFrameSize();
//		
//		System.out.println(bufSize);
//		
//		byte[] buf = new byte[bufSize];
//		ByteList list = new ByteList(bufSize);
//		int numRead = 0;
//		
//		while ((numRead = stream.read(buf, 0, buf.length)) > -1)  {
//			System.out.println("num: " + numRead);
//			for (int i = 0; i < numRead; i++) {
//				list.add(buf[i]);
//			}
//		}
//		return list.asArray();
		
		AudioFormat format = stream.getFormat();
	    int bytesPerFrame = stream.getFormat().getFrameSize();
	    byte[] b = new byte[30000];
		
	    if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) //not sure why this is necessary
	       {
	           bytesPerFrame = 1;
	       } 
	    int i = 0;
	    
	    System.out.println("avail: " + stream.available());
	    
		while(stream.available() > 0)
	    {
	        byte currentByte = (byte)stream.read();
	        b[i] = currentByte; //read bytes to array
	        System.out.println(b[i] + "   " +(i+1)); //test statement
	        i++;
	    }
		
		return b;
		
		//return readAudioFileData(stream);
		
	}
	
	public static byte[] readAudioFileData(AudioInputStream audioInputStream) {
        byte[] data = null;
        try {
            final ByteArrayOutputStream baout = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096 * 2];
            int c;
            while ((c = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
            	
//            	for(int i = 0; i < buffer.length; i++){
//            		System.out.println("[" + i + "] " + buffer[i]);
//           	}
            	
            	//System.out.println("buf " + buffer.length + " " + c);
            	
                baout.write(buffer, 0, c);
            }
            audioInputStream.close();
            baout.close();
            data = baout.toByteArray();

            System.out.println("bl " + data.length);
            
            for(int i = 0; i < data.length; i++){
            	System.out.println("[" + i + "] " + data[i]);
        	}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
	
	/**
	 * Dumps audio data to a temporary file for streaming and returns a
	 * StreamInfo for the stream.
	 * @param data the audio data to write to the temporary file
	 * @return a StreamInfo for the stream
	 */
	private static StreamInfo createFileStream(byte[][] data) {
		//first try to create a file for the data to live in
		File temp = null;
		try {
			temp = File.createTempFile("tiny", "sound");
			//make sure this file will be deleted on exit
			temp.deleteOnExit();
		} catch (IOException e) {
			System.err.println("Failed to create file for streaming!");
			return null;
		}
		//see if we can get the URL for this file
		URL url = null;
		try {
			url = temp.toURI().toURL();
		} catch (MalformedURLException e1) {
			System.err.println("Failed to get URL for stream file!");
			return null;
		}
		//we have the file, now we want to be able to write to it
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(temp),
					(512 * 1024)); //buffer 512kb
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open stream file for writing!");
			return null;
		}
		//write the bytes to the file
		try {
			//write two at a time from each channel
			for (int i = 0; i < data[0].length; i += 2) {
				try {
					//first left
					out.write(data[0], i, 2);
					//then right
					out.write(data[1], i, 2);
				}
				catch (IOException e) {
					//hmm
					System.err.println("Failed writing bytes to stream file!");
					return null;
				}
			}
		}
		finally {
			try {
				out.close();
			} catch (IOException e) {
				//what?
				System.err.println("Failed closing stream file after writing!");
			}
		}
		return new StreamInfo(url, data[0].length);
	}
	
	/**
	 * Iterates through available JavaSound Mixers looking for one that can
	 * provide a line to the speakers.
	 * @return an opened SourceDataLine to the speakers
	 */
	private static SourceDataLine tryGetLine() {
		//first build our line info and get all available mixers
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class,
				TinySoundBetter.FORMAT);
		javax.sound.sampled.Mixer.Info[] mixerInfos =
			AudioSystem.getMixerInfo();
		//iterate through the mixers trying to find a line
		for (int i = 0; i < mixerInfos.length; i++) {
			javax.sound.sampled.Mixer mixer = null;
			try {
				//first try to actually get the mixer
				mixer = AudioSystem.getMixer(mixerInfos[i]);
			}
			catch (SecurityException e) {
				//not much we can do here
			}
			catch (IllegalArgumentException e) {
				//this should never happen since we were told the mixer exists
			}
			//check if we got a mixer and our line is supported
			if (mixer == null || !mixer.isLineSupported(lineInfo)) {
				continue;
			}
			//see if we can actually get a line
			SourceDataLine line = null;
			try {
				line = (SourceDataLine)mixer.getLine(lineInfo);
				//don't try to open if already open
				if (!line.isOpen()) {
					line.open(TinySoundBetter.FORMAT);
				}
			}
			catch (LineUnavailableException e) {
				//we either failed to get or open
				//should we do anything here?
			}
			catch (SecurityException e) {
				//not much we can do here
			}
			//check if we succeeded
			if (line != null && line.isOpen()) {			
				return line;
			}
		}
		//no good
		return null;
	}
	
}
