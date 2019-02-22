package io.github.bluntphenomena.hgjabba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import io.github.bluntphenomena.hgjabba.util.SpectatorUtil;

public class HGSpectatorManager {
	
	private static final long ASSIGN_SPECTATOR_TARGET_DELAY = 40L; // 2 seconds
	
	private HGJabba game;
	private TrackingRangeThread thread;
	
	private HashMap<Player,Player> spectators = new HashMap<Player,Player>();
	
	
	public HGSpectatorManager(HGJabba game) {
		this.game = game;
		thread = new TrackingRangeThread();
		this.game.getServer().getScheduler().scheduleSyncRepeatingTask(this.game, thread, 0L, TrackingRangeThread.PERIOD);
	}
	
	
	public void spectate(Player spectator, Player target) {
		if (spectator == null || target == null)
			return;
		SpectatorUtil.setCamera(spectator, target);
		spectators.put(spectator, target);
		HGChat.send(spectator, ChatColor.LIGHT_PURPLE+"You're now watching "+target.getName());
	}
	
	public void free(Player spectator) {
		if (spectator == null)
			return;
		SpectatorUtil.resetCamera(spectator);
		spectators.remove(spectator);
	}
	
	
	public void assignNewSpectatorTarget(Player player) {
		String team = game.getData().getPlayer(player).getTeam();
		List<UUID> players = new ArrayList<UUID>();
		for (OfflinePlayer p : game.getData().getAliveTeamPlayers(team)) {
			if (p.isOnline() && !p.equals(game.getSpectatorManager().getSpectatorTarget(player)))
				players.add(p.getUniqueId());
		}
		if (players.isEmpty()) {
			game.getSpectatorManager().free(player);
			player.kickPlayer(ChatColor.LIGHT_PURPLE+"None of your teammates is online");
			return;
		}
		Player target = Bukkit.getPlayer(players.get(0));
		game.getSpectatorManager().spectate(player, target);
	}
	
	public void assignNewSpectatorTargetDelay(final Player player) {
		HGChat.send(player, ChatColor.LIGHT_PURPLE+"Please wait a few seconds...");
		Bukkit.getScheduler().scheduleSyncDelayedTask(game, new Runnable() {
			public void run() {
				assignNewSpectatorTarget(player);
			}
		}, ASSIGN_SPECTATOR_TARGET_DELAY);
	}
	
	
	public Set<Player> getAllSpectators() {
		return spectators.keySet();
	}
	
	public Player getSpectatorTarget(Player player) {
		return spectators.get(player);
	}
	
	public Set<Player> getSpectators(Player player) {
		Set<Player> playerSpectators = new HashSet<Player>();
		for (Player p : getAllSpectators()) {
			if (getSpectatorTarget(p).equals(player))
				playerSpectators.add(p);
		}
		return playerSpectators;
	}
	
	
	private class TrackingRangeThread implements Runnable {
		
		private static final long PERIOD = 50;
		
		@Override
		public void run() {
			for (Player p : getAllSpectators()) {
				Player t = getSpectatorTarget(p);
				p.teleport(t);
				SpectatorUtil.setCamera(p, t);
			}
		}
	}
}
