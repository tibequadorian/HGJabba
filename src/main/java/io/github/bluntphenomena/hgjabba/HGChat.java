package io.github.bluntphenomena.hgjabba;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HGChat implements Listener {
	
	private static HGJabba game;
	private static final String prefix = ChatColor.DARK_GRAY+"["+ChatColor.GOLD+"HG"+ChatColor.DARK_GRAY+"] "+ChatColor.GRAY;
	
	public static void initialize(HGJabba plugin) {
		HGChat.game = plugin;
		plugin.getServer().getPluginManager().registerEvents(new HGChat(), plugin);
	}
	
	public static void broadcast(String msg) {
		for (Player p : game.getServer().getOnlinePlayers()) {
			p.sendMessage(msg);
		}
		game.getLogger().info(ChatColor.stripColor(msg));
	}
	public static void broadcastPrefix(String msg) {
		broadcast(prefix + msg);
	}
	
	public static void send(CommandSender sender, String msg) {
		sender.sendMessage(msg);
	}
	public static void sendPrefix(CommandSender sender, String msg) {
		send(sender, prefix + msg);
	}
	
	public static void sendTeam(String team, String msg) {
		for (OfflinePlayer p : game.getData().getTeamPlayers(team))
			send(Bukkit.getPlayer(p.getUniqueId()), msg);
	}
	
	public static void info(String msg) {
		game.getLogger().info(msg);
	}
	
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		event.setFormat(event.getPlayer().getDisplayName()+": "+ChatColor.GRAY+event.getMessage());
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(ChatColor.GREEN+"\u00BB "+ChatColor.RESET+event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage(ChatColor.RED+"\u00AB "+ChatColor.RESET+event.getPlayer().getName());
	}
}
