package io.github.tibequadorian.hgjabba;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class HGCommandManager implements CommandExecutor, TabCompleter {

	private HGJabba game;
	
	public HGCommandManager(HGJabba game) {
		this.game = game;
		this.game.getCommand("hg").setExecutor(this);
		this.game.getCommand("hg").setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 || args[0].equals("help")) {
			if (sender.hasPermission("hgjabba.user"))
				HGChat.send(sender, "/hg help - displays help for /hg");
			if (sender.hasPermission("hgjabba.admin"))
				HGChat.send(sender, "/hg start - starts the game");
			if (sender.hasPermission("hgjabba.admin"))
				HGChat.send(sender, "/hg team create - creates a new team");
			if (sender.hasPermission("hgjabba.user"))
				HGChat.send(sender, "/hg team list - lists all teams");
			if (sender.hasPermission("hgjabba.admin"))
				HGChat.send(sender, "/hg team delete - deletes a new team");
			if (sender.hasPermission("hgjabba.admin"))
				HGChat.send(sender, "/hg player add - adds a player to a team");
			if (sender.hasPermission("hgjabba.user"))
				HGChat.send(sender, "/hg player list - lists all player and their team");
			if (sender.hasPermission("hgjabba.admin"))
				HGChat.send(sender, "/hg player remove - removes a player from team");
			return true;
		} else if (args[0].equals("start")) {
			game.runGame();
			return true;
		} else if (args[0].equals("team")) {
			if (args.length == 1) {
				if (sender.hasPermission("hgjabba.admin"))
					HGChat.send(sender, ChatColor.RED + "Usage: /hg team <create|list|delete>");
				else if (sender.hasPermission("hgjabba.user"))
					HGChat.send(sender, ChatColor.RED + "Usage: /hg team list");
				return true;
			} else if (args[1].equals("create")) {
				if (args.length == 3 || args.length == 4) {
					String team = args[2];
					String color = "white";
					if (args.length == 4)
						color = args[3];
					game.getData().addTeam(team, ChatColor.valueOf(color.toUpperCase()));
					HGChat.send(sender, "The team " + game.getData().getTeam(team).getColor() + team + ChatColor.RESET + " was created");
				} else {
					HGChat.send(sender, ChatColor.RED + "Usage: /hg team create <teamname> [teamcolor]");
				}
				return true;
			} else if (args[1].equals("list")) {
				Set<String> teams = game.getData().getTeams();
				HGChat.send(sender, "There are " + teams.size() + " teams");
				for (String t : teams) {
					HGChat.send(sender, "- " + game.getData().getTeam(t).getColor() + t + ChatColor.RESET + " (" + game.getData().getTeamPlayers(t).size() + ")");
				}
				return true;
			} else if (args[1].equals("delete")) {
				if (args.length == 3) {
					String team = args[2];
					ChatColor teamcolor = game.getData().getTeam(team).getColor();
					game.getData().removeTeam(team);
					HGChat.send(sender, "The team " + teamcolor + team + ChatColor.RESET + " was deleted");
				} else {
					HGChat.send(sender, ChatColor.RED + "Usage: /hg team delete <team>");
				}
				return true;
			}
		} else if (args[0].equals("player")) {
			if (args.length == 1) {
				if (sender.hasPermission("hgjabba.admin"))
					HGChat.send(sender, ChatColor.RED + "Usage: /hg player <add|list|remove>");
				else if (sender.hasPermission("hgjabba.user"))
					HGChat.send(sender, ChatColor.RED + "Usage: /hg player list");
				return false;
			} else if (args[1].equals("add")) {
				if (args.length == 4) {
					String playername = args[2];
					String team = args[3];
					Player player = Bukkit.getPlayer(playername);
					if (player == null) {
						HGChat.send(sender, ChatColor.RED + playername + " is currently not online!");
						return true;
					}
					game.getData().addPlayer(player, team);
					HGChat.send(sender, "Added " + playername + " to the team " + game.getData().getTeam(team).getColor() + team);
				} else {
					HGChat.send(sender, ChatColor.RED + "Usage: /hg player add <player> <team>");
				}
				return true;
			} else if (args[1].equals("list")) {
				Set<OfflinePlayer> players = game.getData().getIngamePlayers();
				HGChat.send(sender, "There are " + players.size() + " players in the game");
				for (OfflinePlayer p : players) {
					String team = game.getData().getPlayer(p).getTeam();
					HGChat.send(sender, "- " + p.getName() + " [" + game.getData().getTeam(team).getColor() + team + ChatColor.RESET + "]");
				}
				return true;
			} else if (args[1].equals("remove")) {
				if (args.length == 3) {
					String playername = args[2];
					Player player = Bukkit.getPlayer(playername);
					if (player == null) {
						HGChat.send(sender, ChatColor.RED + playername + " is currently not online!");
						return true;
					}
					String team = game.getData().getPlayer(player).getTeam();
					game.getData().removePlayer(player);
					HGChat.send(sender, "Removed " + playername + " from the team " + game.getData().getTeam(team).getColor() + team);
				} else {
					HGChat.send(sender, ChatColor.RED + "Usage: /hg player remove <player>");
				}
				return true;
			}
		}
		return false;
	}
	
	private static final String[] ADMIN_HG_SUGGESTIONS = { "help", "player", "start", "team" };
	private static final String[] USER_HG_SUGGESTIONS = { "help", "player", "team" };
	private static final String[] ADMIN_TEAM_SUGGESTIONS = { "create", "delete", "list" };
	private static final String[] USER_TEAM_SUGGESTIONS = { "list" };
	private static final String[] ADMIN_PLAYER_SUGGESTIONS = { "add", "list", "remove" };
	private static final String[] USER_PLAYER_SUGGESTIONS = { "list" };
	private static final String[] TEAMCOLOR_SUGGESTIONS = {"black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
				"dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white" };

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> suggestions = new ArrayList<String>();
		List<String> completions = new ArrayList<String>();
		if (args.length == 1) {
			if (sender.hasPermission("hgjabba.admin"))
				suggestions = new ArrayList<String>(Arrays.asList(ADMIN_HG_SUGGESTIONS));
			else if (sender.hasPermission("hgjabba.user"))
				suggestions = new ArrayList<String>(Arrays.asList(USER_HG_SUGGESTIONS));
			StringUtil.copyPartialMatches(args[0], suggestions, completions);
		} else if (args.length == 2) {
			if (args[0].equals("team")) {
				if (sender.hasPermission("hgjabba.admin"))
					suggestions = new ArrayList<String>(Arrays.asList(ADMIN_TEAM_SUGGESTIONS));
				else if (sender.hasPermission("hgjabba.user"))
					suggestions = new ArrayList<String>(Arrays.asList(USER_TEAM_SUGGESTIONS));
			} else if (args[0].equals("player")) {
				if (sender.hasPermission("hgjabba.admin"))
					suggestions = new ArrayList<String>(Arrays.asList(ADMIN_PLAYER_SUGGESTIONS));
				else if (sender.hasPermission("hgjabba.user"))
					suggestions = new ArrayList<String>(Arrays.asList(USER_PLAYER_SUGGESTIONS));
			}
			StringUtil.copyPartialMatches(args[1], suggestions, completions);
		} else if (args.length == 3) {
			if (args[0].equals("team") && args[1].equals("delete")) {
				if (sender.hasPermission("hgjabba.admin")) {
					suggestions.addAll(game.getData().getTeams());
				}
			} else if (args[0].equals("player") && (args[1].equals("add") || args[1].equals("remove"))) {
				if (sender.hasPermission("hgjabba.admin")) {
					for (Player p : game.getServer().getOnlinePlayers())
						suggestions.add(p.getName());
				}
			}
			StringUtil.copyPartialMatches(args[2], suggestions, completions);
		} else if (args.length == 4) {
			if (args[0].equals("team") && args[1].equals("create")) {
				if (sender.hasPermission("hgjabba.admin")) {
					suggestions = new ArrayList<String>(Arrays.asList(TEAMCOLOR_SUGGESTIONS));
				}
			} else if (args[0].equals("player") && args[1].equals("add")) {
				if (sender.hasPermission("hgjabba.admin")) {
					suggestions.addAll(game.getData().getTeams());
				}
			}
			StringUtil.copyPartialMatches(args[3], suggestions, completions);
		}
		Collections.sort(completions);
		return completions;
	}
}
