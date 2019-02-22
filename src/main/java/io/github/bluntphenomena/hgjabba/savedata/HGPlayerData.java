package io.github.bluntphenomena.hgjabba.savedata;

public class HGPlayerData {
	
	private boolean alive;
	private String team;
	
	
	public HGPlayerData(boolean alive, String team) {
		this.alive = alive;
		this.team = team;
	}
	
	
	public boolean isAlive() {
		return alive;
	}
	public void setAlive(boolean alive) {
		this.alive = alive;
	}
	
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
}
