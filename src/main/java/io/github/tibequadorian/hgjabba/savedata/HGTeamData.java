package io.github.tibequadorian.hgjabba.savedata;

import org.bukkit.ChatColor;

public class HGTeamData {
	
	private final ChatColor color;
	private boolean alive;
	
	
	public HGTeamData(ChatColor color, boolean alive) {
		this.color = color;
		this.alive = alive;
	}
	
	
	public ChatColor getColor() {
		return color;
	}
	
	public boolean isAlive() {
		return alive;
	}
	public void extinguish() {
		this.alive = false;
	}
}
