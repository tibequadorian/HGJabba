package io.github.bluntphenomena.hgjabba;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class HGThread implements Runnable {
	
	private HGJabba game;
	
	private long time;
	private long countdown;
	
	
	public HGThread(HGJabba game) {
		this.game = game;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this.game, this, 0L, 20);
	}
	
	
	@Override
	public void run() {
		game.checkPlayers();
		
		if (game.getGameState() == HGGameState.WAITING_FALLBACK) {
			if (countdown > 0 && (countdown % 10 == 0 || countdown <= 5))
				HGChat.broadcast(ChatColor.AQUA+""+countdown+ChatColor.DARK_AQUA+" seconds until start");
			if (countdown == 0) {
				HGChat.broadcast(ChatColor.GREEN+"The game is running now!");
				game.setRunningState();
			}
			countdown--;
		}
		
		if (game.getGameState() == HGGameState.RUNNING_FALLBACK) {
			if (countdown > 0 && (countdown % 10 == 0 || countdown <= 5))
				HGChat.broadcast(ChatColor.AQUA+""+countdown+ChatColor.DARK_AQUA+" seconds until pause");
			if (countdown == 0) {
				kickAll();
				game.setWaitingState();
			}
			countdown--;
		}
		
		if (game.getData().isStarted() && !game.getGameState().isIdle()) {
			
			if (time == game.getWorldborder().getStartMovingTime()) {
				game.getWorldborder().unpause();
				HGChat.broadcast(ChatColor.RED+"The worldborder is now shrinking!");
			}
			if (time == game.getGracePeriod()) {
				HGChat.broadcast(ChatColor.RED+"The grace period has ended!");
			}
			
			game.getScoreboard().update();
			
			time++;
		}
	}
	
	private void kickAll() {
		for (Player p : game.getServer().getOnlinePlayers()) {
			p.kickPlayer(ChatColor.RED+"The game was paused!");
		}
	}
	
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public void setCountdown(long countdown) {
		this.countdown = countdown;
	}
}
