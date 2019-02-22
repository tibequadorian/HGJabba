package io.github.bluntphenomena.hgjabba.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.bluntphenomena.hgjabba.HGJabba;

public class HGIdleStateListener implements Listener {
	
	private HGJabba game;
	
	
	public HGIdleStateListener(HGJabba game) {
		this.game = game;
	}
	
	
	public void register() {
		this.game.getServer().getPluginManager().registerEvents(this, this.game);
	}
	
	public void unregister() {
		HandlerList.unregisterAll(this);
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (game.getGameState().isIdle()) {
			Player player = event.getPlayer();
			if (game.getData().getPlayer(player).isAlive())
				player.setGameMode(GameMode.ADVENTURE);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (game.getGameState().isIdle()) {
			Player player = event.getPlayer();
			if (game.getData().getPlayer(player).isAlive())
				player.setGameMode(GameMode.SURVIVAL);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (game.getGameState().isIdle()) {
			Location from = event.getFrom();
			Location to = event.getTo();
			if (from.getX() != to.getX() ||
					from.getY() != to.getY() ||
					from.getZ() != to.getZ()) {
				event.getPlayer().teleport(new Location(event.getPlayer().getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch()));
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (game.getGameState().isIdle() || !game.getData().isStarted()) {
			if (event.getEntity() instanceof Player)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (game.getGameState().isIdle()) {
			if (event.getDamager() instanceof Player)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onRegainHealth(EntityRegainHealthEvent event) {
		if (game.getGameState().isIdle()) {
			if (event.getEntity() instanceof Player)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent event) {
		if (game.getGameState().isIdle() || !game.getData().isStarted()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemThrow(PlayerDropItemEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryInteract(InventoryClickEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		if (game.getGameState().isIdle()) {
			event.setCancelled(true);
		}
	}
}
