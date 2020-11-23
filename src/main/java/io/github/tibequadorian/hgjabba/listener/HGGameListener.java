package io.github.tibequadorian.hgjabba.listener;

import io.github.tibequadorian.hgjabba.HGChat;
import io.github.tibequadorian.hgjabba.HGJabba;
import io.github.tibequadorian.hgjabba.savedata.HGPlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.PortalCreateEvent;

import java.util.*;

public class HGGameListener implements Listener {

	private final HGJabba game;

	public HGGameListener(HGJabba game) {
		this.game = game;
	}
	
	public void register() {
		game.getServer().getPluginManager().registerEvents(this, game);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore())
			player.teleport(game.getWorld().getSpawnLocation());
		game.updatePlayerDisplayName(player);
		HGPlayerData playerdata = game.getData().getPlayer(player);
		if (playerdata == null)
			return;
		String team = playerdata.getTeam();
		if (!playerdata.isAlive() && game.getData().getTeam(team).isAlive()) {
			game.getSpectatorManager().assignNewSpectatorTargetDelay(player);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		for (Player p : game.getSpectatorManager().getSpectators(player))
			game.getSpectatorManager().assignNewSpectatorTarget(p);
		if (game.getSpectatorManager().getSpectatorTarget(player) != null)
			game.getSpectatorManager().free(player);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(event.getPlayer().getLocation());
		Player player = event.getPlayer();
		HGPlayerData playerdata = game.getData().getPlayer(player);
		if (playerdata == null)
			return;
		String team = playerdata.getTeam();
		if (game.getData().getTeam(team).isAlive()) {
			game.getSpectatorManager().assignNewSpectatorTargetDelay(player);
		}
	}

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		HGPlayerData playerdata = game.getData().getPlayer(player);
		if (playerdata == null)
			return;
		if (!player.isSneaking() && game.getSpectatorManager().getSpectatorTarget(player) != null) {
			String team = playerdata.getTeam();
			if (game.getData().getTeam(team).isAlive()) {
				UUID currentTarget = game.getSpectatorManager().getSpectatorTarget(player).getUniqueId();
				List<UUID> players = new ArrayList<UUID>();
				for (OfflinePlayer p : game.getData().getAliveTeamPlayers(team)) {
					if (p.isOnline())
						players.add(p.getUniqueId());
				}
				Iterator<UUID> iterator = players.iterator();
				while (iterator.hasNext()) {
					UUID next = iterator.next();
					if (next.equals(currentTarget)) {
						UUID newTarget;
						if (iterator.hasNext())
							newTarget = iterator.next();
						else
							newTarget = players.get(0);
						game.getSpectatorManager().spectate(player, Bukkit.getPlayer(newTarget));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Player killer = player.getKiller();
		game.getData().getPlayer(player).extinguish();
		String team = game.getData().getPlayer(player).getTeam();
		if (game.getData().getAliveTeamPlayers(team).isEmpty())
			game.getData().getTeam(team).extinguish();
		
		player.setGameMode(GameMode.SPECTATOR);
		for (Player p : game.getServer().getOnlinePlayers()) {
			p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
		}
		event.setDeathMessage(null);
		ChatColor playerTeamColor = game.getData().getTeam(team).getColor();
		if (killer == null) {
			HGChat.broadcast(playerTeamColor+player.getName()+ChatColor.DARK_RED+" died");
		} else {
			ChatColor killerTeamColor = game.getData().getTeam(game.getData().getPlayer(killer).getTeam()).getColor();
			HGChat.broadcast(playerTeamColor+player.getName()+ChatColor.DARK_RED+" has been eliminated by "+killerTeamColor+killer.getName());
		}
		if (!game.getData().getTeam(team).isAlive()) {
			HGChat.broadcast(ChatColor.DARK_RED+"The team "+game.getData().getTeam(team).getColor()+team+ChatColor.DARK_RED+" has been eliminated");
			for (Player p : game.getSpectatorManager().getSpectators(player))
				game.getSpectatorManager().free(p);
		} else {
			for (Player p : game.getSpectatorManager().getSpectators(player))
				game.getSpectatorManager().assignNewSpectatorTarget(p);
		}
		
		if (game.getData().getAliveTeams().size() == 1) {
			String winnerteam = game.getData().getAliveTeams().iterator().next();
			HGChat.broadcast(ChatColor.GOLD+"The team "+game.getData().getTeam(winnerteam).getColor()+winnerteam+ChatColor.GOLD+" has won!");
			/*
			Set<Player> alive_players = new HashSet<Player>();
			for (OfflinePlayer p : game.getData().getAlivePlayers())
				alive_players.add(Bukkit.getPlayer(p.getUniqueId()));
			Bukkit.getScheduler().scheduleSyncRepeatingTask(game, new Runnable() {
				public void run() {
					// TODO: fireworks
				}
			}, 0L, 10L);
			 */
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (game.getSpectatorManager().getSpectatorTarget(player) != null) {
			if (event.getCause() == TeleportCause.SPECTATE) {
				event.setCancelled(true);
				HGChat.send(player, ChatColor.RED+"You're not allowed to teleport");
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Player damager = null, player = null;
		if (event.getDamager() instanceof Player)
			damager = (Player) event.getDamager();
		if (event.getEntity() instanceof Player)
			player = (Player) event.getEntity();
		if (damager == null || player == null)
			return;
		
		if (game.getThread().getTime() < game.getGracePeriod()) {
			event.setCancelled(true);
		}
		
		HGPlayerData damager_data = game.getData().getPlayer(damager);
		HGPlayerData player_data = game.getData().getPlayer(player);
		if (damager_data == null || player_data == null)
			return;
		if (damager_data.getTeam().equals(player_data.getTeam())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerPortalCreate(PortalCreateEvent event) {
		if (game.getData().isStarted())
			event.setCancelled(true);
	}

	/*
	@EventHandler
	public void onPlayerPortalEnter(PlayerPortalEvent event) {
		event.useTravelAgent(true);
		TravelAgent travelAgent = event.getPortalTravelAgent();
		travelAgent.setCanCreatePortal(true);
		
		World wld = null;
		if (event.getPlayer().getWorld().getEnvironment() == Environment.NORMAL)
			wld = game.getWorldNether();
		else if (event.getPlayer().getWorld().getEnvironment() == Environment.NETHER)
			wld = game.getWorld();
		if (wld == null)
			return;
		
		Location loc = travelAgent.findOrCreate(new Location(wld, 0, 64, 0));
		event.setTo(loc);
	}
	 */
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (b.getType() == Material.NETHER_PORTAL)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockExplode(BlockExplodeEvent event) {
		for (Block b : event.blockList()) {
			if (b.getType() == Material.NETHER_PORTAL)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		BlockFace face = event.getBlockFace();
		Location blockLoc = event.getBlockClicked().getLocation().add(face.getModX(), face.getModY(), face.getModZ());
		if (game.getWorld().getBlockAt(blockLoc).getType() == Material.NETHER_PORTAL)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getType() == Material.OBSIDIAN && game.getData().isStarted()) {
			if (game.getWorld().getBlockAt(block.getLocation().add(-1, 0, 0)).getType() == Material.NETHER_PORTAL
					|| game.getWorld().getBlockAt(block.getLocation().add(0, -1, 0)).getType() == Material.NETHER_PORTAL
					|| game.getWorld().getBlockAt(block.getLocation().add(0, 0, -1)).getType() == Material.NETHER_PORTAL
					|| game.getWorld().getBlockAt(block.getLocation().add(1, 0, 0)).getType() == Material.NETHER_PORTAL
					|| game.getWorld().getBlockAt(block.getLocation().add(0, 1, 0)).getType() == Material.NETHER_PORTAL
					|| game.getWorld().getBlockAt(block.getLocation().add(0, 0, 1)).getType() == Material.NETHER_PORTAL) {
				event.setCancelled(true);
				Player player = event.getPlayer();
				HGChat.send(player, ChatColor.RED+"You're not allowed to break this block");
			}
		}
	}
}
