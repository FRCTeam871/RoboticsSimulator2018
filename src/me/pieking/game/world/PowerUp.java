package me.pieking.game.world;

import java.util.ArrayList;
import java.util.List;

import me.pieking.game.world.Balance.Team;

public class PowerUp {

	private Team using = Team.NONE;
	private Team queued = Team.NONE;
	private List<Team> used = new ArrayList<>();
	private int timer = 0;
	private int level = 0;
	
	private int redLevel = 0;
	private int blueLevel = 0;

	public Team getUsing() {
		return using;
	}

	public void setUsing(Team using) {
		this.using = using;
	}

	public Team getQueued() {
		return queued;
	}

	public void setQueued(Team queued) {
		this.queued = queued;
	}

	public List<Team> getUsed() {
		return used;
	}

	public void addUsed(Team used) {
		this.used.add(used);
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void reset() {
		using = Team.NONE;
		queued = Team.NONE;
		timer = 0;
		used.clear();
	}

}
