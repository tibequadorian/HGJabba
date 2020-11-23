package io.github.tibequadorian.hgjabba;

import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class HGScoreboard {

	private HGJabba game;
	private Scoreboard scoreboard;
	private Objective sidebar;

	public HGScoreboard(HGJabba game) {
		this.game = game;
		initialize();
	}

	private static void setScore(Objective objective, String name, int value) {
		Score score = objective.getScore(name);
		score.setScore(value);
	}

	public void initialize() {
		scoreboard = game.getServer().getScoreboardManager().getMainScoreboard();
		sidebar = scoreboard.getObjective("sidebar");
		if (sidebar == null)
			sidebar = scoreboard.registerNewObjective("sidebar", "dummy");
		sidebar.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "JabbaHG");
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		for (String entry : scoreboard.getEntries())
			scoreboard.resetScores(entry);
	}

	public void update() {
		// clear scoreboard
		for (String entry : scoreboard.getEntries())
			scoreboard.resetScores(entry);

		// time
		setScore(sidebar, ChatColor.GREEN+""+ChatColor.BOLD+"Time:", 9);
		long time = game.getThread().getTime();
		setScore(sidebar, ChatColor.AQUA+""+formatTime(time), 8);

		// worldborder
		setScore(sidebar, ChatColor.BLUE + "" + ChatColor.BOLD + "Borderradius:", 7);
		setScore(sidebar, ChatColor.LIGHT_PURPLE + "" + ((int) game.getWorldborder().getRadius()) + " blocks", 6);

		// teams
		setScore(sidebar, ChatColor.RED + "" + ChatColor.BOLD + "Teams:", 5);
		int val = 4;
		for (String team : game.getData().getAliveTeams()) {
			setScore(sidebar, game.getData().getTeam(team).getColor() + team, val--);
		}
	}
	
	private String formatTime(long time) {
		long seconds = time % 60;
		long minutes = (time / 60) % 60;
		long hours = (time / 3600) % 3600;
		return hours+":"+(minutes < 10 ? "0" : "")+minutes+":"+(seconds < 10 ? "0" : "")+seconds;
	}
}
