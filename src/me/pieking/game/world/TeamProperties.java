package me.pieking.game.world;

import java.util.HashMap;

public class TeamProperties {
	private Switch teamSwitch;
	private GameObject exchangeSensor;
	private int cubeStorage = 0;
	private HashMap<Pentalty, Integer> penalties = new HashMap<Pentalty, Integer>();
	private int switchScoreMod = 1;
	private int scaleScoreMod = 1;
	private int score = 0;
	private int boostLevel = 0;
	private int forceLevel = 0;
	private boolean usedLevitate = false;

	public TeamProperties(Switch teamSwitch, GameObject exchangeSensor) {
		this.teamSwitch = teamSwitch;
		this.exchangeSensor = exchangeSensor;
		
		penalties.put(Pentalty.FOUL, 0);
		penalties.put(Pentalty.TECH_FOUL, 0);
	}

	public Switch getSwitch() {
		return teamSwitch;
	}

	public GameObject getExchangeSensor() {
		return exchangeSensor;
	}

	public int getCubeStorage() {
		return cubeStorage;
	}

	public void setCubeStorage(int cubeStorage) {
		this.cubeStorage = cubeStorage;
	}
	
	public void addCubeStorage(int cubeStorage) {
		this.cubeStorage += cubeStorage;
	}
	
	public void removeCubeStorage(int cubeStorage) {
		this.cubeStorage = Math.max(0, cubeStorage);
	}
	
	public HashMap<Pentalty, Integer> getPenalties() {
		return penalties;
	}
	
	public int getPenaltyCount(Pentalty type){
		return penalties.get(type);
	}
	
	public void setPenalty(Pentalty type, int count){
		penalties.put(type, count);
	}
	
	public void addPenalty(Pentalty type, int count){
		penalties.put(type, penalties.get(type) + count);
	}

	public int getSwitchScoreMod() {
		return switchScoreMod;
	}

	public void setSwitchScoreMod(int switchScoreMod) {
		this.switchScoreMod = switchScoreMod;
	}

	public int getScaleScoreMod() {
		return scaleScoreMod;
	}

	public void setScaleScoreMod(int scaleScoreMod) {
		this.scaleScoreMod = scaleScoreMod;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	public void addScore(int score){
		this.score += score;
	}

	public boolean getUsedLevitate() {
		return usedLevitate;
	}

	public void setUsedLevitate(boolean usedLevitate) {
		this.usedLevitate = usedLevitate;
	}

	public int getBoostLevel() {
		return boostLevel;
	}

	public void setBoostLevel(int boostLevel) {
		this.boostLevel = boostLevel;
	}

	public int getForceLevel() {
		return forceLevel;
	}

	public void setForceLevel(int forceLevel) {
		this.forceLevel = forceLevel;
	}
}
