package io.github.bluntphenomena.hgjabba.savedata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.github.bluntphenomena.hgjabba.HGJabba;
import io.github.bluntphenomena.hgjabba.HGWorldborder;

public class HGSaveData {
	
	private HGJabba game;
	private FileConfiguration config;
	
	private boolean setup;
	private boolean running;
	private boolean started;
	private HashMap<UUID,HGPlayerData> ingame_players = new HashMap<UUID,HGPlayerData>();
	private HashMap<String,HGTeamData> teams = new HashMap<String,HGTeamData>();
	
	
	public HGSaveData(HGJabba game) {
		this.game = game;
		this.config = game.getConfig();
	}
	
	
	public void setDefaults() {
		config.addDefault("setup", false);
		config.addDefault("running", false);
		config.addDefault("started", false);
		config.addDefault("time", 0L);
		config.addDefault("worldborder.initial_radius", HGWorldborder.DEFAULT_INITIAL_RADIUS);
		config.addDefault("worldborder.final_radius", HGWorldborder.DEFAULT_FINAL_RADIUS);
		config.addDefault("worldborder.start_moving_time", HGWorldborder.DEFAULT_START_MOVING_TIME);
		config.addDefault("worldborder.stop_moving_time", HGWorldborder.DEFAULT_STOP_MOVING_TIME);
		config.options().copyDefaults(true);
		game.saveConfig();
	}
	
	public void loadData() {
		setup = config.getBoolean("setup");
		running = config.getBoolean("running");
		started = config.getBoolean("started");
		game.getThread().setTime(config.getLong("time"));
		ConfigurationSection worldborderSection = config.getConfigurationSection("worldborder");
		if (worldborderSection != null) {
			double initial_radius = config.getDouble("worldborder.initial_radius");
			double final_radius = config.getDouble("worldborder.final_radius");
			long start_moving_time = config.getLong("worldborder.start_moving_time");
			long stop_moving_time = config.getLong("worldborder.stop_moving_time");
			game.getWorldborder().setValues(initial_radius, final_radius, start_moving_time, stop_moving_time);
		}
		ingame_players = new HashMap<UUID,HGPlayerData>();
		ConfigurationSection playersSection = config.getConfigurationSection("players");
		if (playersSection != null) {
			Set<String> players = playersSection.getKeys(false);
			for (String p : players) {
				UUID uuid = UUID.fromString(p);
				boolean alive = playersSection.getBoolean(p+".alive");
				String team = playersSection.getString(p+".team");
				ingame_players.put(uuid, new HGPlayerData(alive, team));
			}
		}
		ConfigurationSection teamsSection = config.getConfigurationSection("teams");
		if (teamsSection != null) {
			Set<String> teamnames = teamsSection.getKeys(false);
			for (String t : teamnames) {
				ChatColor color = ChatColor.valueOf(teamsSection.getString(t+".color").toUpperCase());
				boolean alive = teamsSection.getBoolean(t+".alive");
				teams.put(t, new HGTeamData(color, alive));
			}
		}
	}
	
	public void saveData() {
		config.set("setup", setup);
		config.set("running", running);
		config.set("started", started);
		config.set("time", game.getThread().getTime());
		config.set("worldborder", null);
		config.set("worldborder.initial_radius", game.getWorldborder().getInitialRadius());
		config.set("worldborder.final_radius", game.getWorldborder().getFinalRadius());
		config.set("worldborder.start_moving_time", game.getWorldborder().getStartMovingTime());
		config.set("worldborder.stop_moving_time", game.getWorldborder().getStopMovingTime());
		config.set("players", null);
		for (OfflinePlayer op : getIngamePlayers()) {
			String p = op.getUniqueId().toString();
			config.set("players."+p+".alive", getPlayer(op).isAlive());
			config.set("players."+p+".team", getPlayer(op).getTeam());
		}
		config.set("teams", null);
		for (String t : getTeams()) {
			config.set("teams."+t+".color", getTeam(t).getColor().name().toLowerCase());
			config.set("teams."+t+".alive", getTeam(t).isAlive());
		}
		game.saveConfig();
	}
	
	
	public boolean isSetup() {
		return setup;
	}
	public void setSetup(boolean setup) {
		this.setup = setup;
	}
	
	public boolean isRunning() {
		return running;
	}
	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public boolean isStarted() {
		return started;
	}
	public void setStarted(boolean started) {
		this.started = true;
	}
	
	
	public Set<OfflinePlayer> getIngamePlayers() {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for (UUID id : ingame_players.keySet()) {
			players.add(Bukkit.getOfflinePlayer(id));
		}
		return players;
	}
	
	public Set<OfflinePlayer> getAlivePlayers() {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for (UUID id : ingame_players.keySet()) {
			if (ingame_players.get(id).isAlive())
				players.add(Bukkit.getOfflinePlayer(id));
		}
		return players;
	}
	
	public Set<Player> getOnlineAlivePlayers() {
		Set<Player> online_alive_players = new HashSet<Player>();
		for (UUID id : ingame_players.keySet()) {
			if (ingame_players.get(id).isAlive() && Bukkit.getOfflinePlayer(id).isOnline())
				online_alive_players.add(Bukkit.getPlayer(id));
		}
		return online_alive_players;
	}
	
	public Set<OfflinePlayer> getTeamPlayers(String team) {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for (OfflinePlayer p : getIngamePlayers()) {
			if (getPlayer(p).getTeam().equals(team))
				players.add(p);
		}
		return players;
	}
	
	public Set<OfflinePlayer> getAliveTeamPlayers(String team) {
		Set<OfflinePlayer> players = new HashSet<OfflinePlayer>();
		for (OfflinePlayer p : getTeamPlayers(team)) {
			if (getPlayer(p).isAlive())
				players.add(p);
		}
		return players;
	}
	
	public HGPlayerData getPlayer(OfflinePlayer player) {
		return ingame_players.get(player.getUniqueId());
	}
	
	public void addPlayer(OfflinePlayer player, String team) {
		ingame_players.put(player.getUniqueId(), new HGPlayerData(true, team));
		game.updatePlayerDisplayName(Bukkit.getPlayer(player.getUniqueId()));
	}
	
	public void removePlayer(OfflinePlayer player) {
		ingame_players.remove(player.getUniqueId());
		game.updatePlayerDisplayName(Bukkit.getPlayer(player.getUniqueId()));
	}
	
	
	public Set<String> getTeams() {
		return teams.keySet();
	}
	
	public Set<String> getAliveTeams() {
		Set<String> alive_teams = new HashSet<String>();
		for (String t : teams.keySet()) {
			if (teams.get(t).isAlive())
				alive_teams.add(t);
		}
		return alive_teams;
	}
	
	public HGTeamData getTeam(String team) {
		return teams.get(team);
	}
	
	public void addTeam(String team, ChatColor color) {
		teams.put(team, new HGTeamData(color, true));
	}
	
	public void removeTeam(String team) {
		teams.remove(team);
		for (OfflinePlayer p : getTeamPlayers(team))
			removePlayer(p);
	}
}
