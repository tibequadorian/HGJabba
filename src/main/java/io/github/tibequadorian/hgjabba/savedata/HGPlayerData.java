package io.github.tibequadorian.hgjabba.savedata;

public class HGPlayerData {
	
	private boolean alive;
	private final String team;
	
	public HGPlayerData(boolean alive, String team) {
		this.alive = alive;
		this.team = team;
	}
	
	public boolean isAlive() {
		return alive;
	}
	public void extinguish() {
		this.alive = false;
	}
	
	public String getTeam() {
		return team;
	}
}
