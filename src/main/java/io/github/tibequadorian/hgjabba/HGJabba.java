package io.github.tibequadorian.hgjabba;

import io.github.tibequadorian.hgjabba.listener.HGGameListener;
import io.github.tibequadorian.hgjabba.listener.HGIdleStateListener;
import io.github.tibequadorian.hgjabba.savedata.HGSaveData;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.tibequadorian.hgjabba.savedata.HGPlayerData;

public class HGJabba extends JavaPlugin implements Listener {

    private static final long WAITING_FALLBACK_TIME = 30;
    private static final long INGAME_FALLBACK_TIME = 30;
    private static final long DEFAULT_GRACE_PERIOD = 300;

    private HGGameState gameState;
    private HGSaveData gameData;
    private HGThread gameThread;
    private HGScoreboard gameScoreboard;
    private HGWorldborder gameWorldborder;
    private HGCommandManager commandManager;
    private HGSpectatorManager spectatorManager;
    private HGGameListener gameListener;
    private HGIdleStateListener idleStateListener;

    private final long grace_period = DEFAULT_GRACE_PERIOD;

    @Override
    public void onEnable() {
        // INITIALIZATION
        gameState = HGGameState.PREGAME;
        gameData = new HGSaveData(this);
        gameThread = new HGThread(this);
        gameScoreboard = new HGScoreboard(this);
        gameWorldborder = new HGWorldborder(this);
        commandManager = new HGCommandManager(this);
        spectatorManager = new HGSpectatorManager(this);
        gameListener = new HGGameListener(this);
        idleStateListener = new HGIdleStateListener(this);
        HGChat.initialize(this);
        // LOAD
        gameData.setDefaults();
        gameData.loadData();
        gameListener.register();
        idleStateListener.register();

        // auto setup
        if (!gameData.isSetup()) {
            setupGame();
        }
        // continue if running
        if (gameData.isRunning()) {
            setWaitingState();
        }
    }

    @Override
    public void onDisable() {
        gameData.saveData();
    }

    public void setupGame() {
        // create safespace
        World world = getWorld();
        for (int x = -16; x < 16; x++) {
            for (int z = -16; z < 16; z++) {
                world.getBlockAt(x, 124, z).setType(Material.BARRIER);
                if (x == -16 || x == 15 || z == -16 || z == 15) {
                    world.getBlockAt(x, 125, z).setType(Material.BARRIER);
                    world.getBlockAt(x, 126, z).setType(Material.BARRIER);
                    world.getBlockAt(x, 127, z).setType(Material.BARRIER);
                }
            }
        }
        world.setSpawnLocation(0, 125, 0);
        world.setFullTime(0);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        gameData.setGamestage(HGGamestage.SETUP);
    }

    public void runGame() {
        setWaitingState();
        HGChat.broadcastPrefix(ChatColor.GOLD+"The game will begin when all players are connected...");
        gameData.setGamestage(HGGamestage.RUNNING);
    }

    private void startGame() {
        World world = getWorld();
        // destroy safespace area
        for (int x = -16; x < 16; x++) {
            for (int z = -16; z < 16; z++) {
                world.getBlockAt(x, 124, z).setType(Material.AIR);
                if (x == -16 || x == 15 || z == -16 || z == 15) {
                    world.getBlockAt(x, 125, z).setType(Material.AIR);
                    world.getBlockAt(x, 126, z).setType(Material.AIR);
                    world.getBlockAt(x, 127, z).setType(Material.AIR);
                }
            }
        }
        world.setFullTime(0);
        for (OfflinePlayer p : gameData.getIngamePlayers()) {
            Player player = (Player)p;
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5*20, 12, false, false));
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setExhaustion(0);
            player.setFoodLevel(20);
            player.setSaturation(5);
        }
        gameWorldborder.start();
        gameThread.setTime(0L);
        gameData.setGamestage(HGGamestage.STARTED);
    }


    private boolean allPlayersOnline() {
        for (OfflinePlayer p : gameData.getAlivePlayers()) {
            if (!p.isOnline())
                return false;
        }
        return true;
    }

    public void setWaitingState() {
        getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        gameWorldborder.pause();
        idleStateListener.register();
        for (Player p : gameData.getOnlineAlivePlayers())
            p.setGameMode(GameMode.ADVENTURE);
        gameState = HGGameState.WAITING;
    }
    public void setWaitingFallbackState() {
        gameThread.setCountdown(WAITING_FALLBACK_TIME);
        gameState = HGGameState.WAITING_FALLBACK;
    }
    public void setRunningState() {
        if (!gameData.isStarted())
            startGame();
        getWorld().setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        if (gameThread.getTime() >= gameWorldborder.getStartMovingTime())
            gameWorldborder.unpause();
        idleStateListener.unregister();
        for (Player p : gameData.getOnlineAlivePlayers())
            p.setGameMode(GameMode.SURVIVAL);
        gameState = HGGameState.RUNNING;
    }
    public void setRunningFallbackState() {
        gameThread.setCountdown(INGAME_FALLBACK_TIME);
        gameState = HGGameState.RUNNING_FALLBACK;
    }

    public void checkPlayers() {
        boolean allPlayersOnline = allPlayersOnline();
        if (gameState == HGGameState.WAITING && allPlayersOnline) {
            HGChat.broadcast(ChatColor.GREEN+"All players are connected now!");
            setWaitingFallbackState();
        } else if (gameState == HGGameState.WAITING_FALLBACK && !allPlayersOnline) {
            HGChat.broadcast(ChatColor.RED+"Not all players are connected!");
            setWaitingState();
        } else if (gameState == HGGameState.RUNNING && !allPlayersOnline) {
            HGChat.broadcast(ChatColor.RED+"Not all players are connected!");
            setRunningFallbackState();
        } else if (gameState == HGGameState.RUNNING_FALLBACK && allPlayersOnline) {
            HGChat.broadcast(ChatColor.GREEN+"All players are connected again!");
            setRunningState();
        }
    }

    public void updatePlayerDisplayName(Player player) {
        if (player == null)
            return;
        HGPlayerData pd = gameData.getPlayer(player);
        String displayName = player.getName();
        if (pd != null) {
            String team = pd.getTeam();
            displayName = ChatColor.DARK_GRAY+"["+gameData.getTeam(team).getColor()+team+ChatColor.DARK_GRAY+"] "+ChatColor.RESET+player.getName();
        }
        player.setDisplayName(displayName);
        player.setPlayerListName(displayName);
    }


    // GETTER AND SETTER

    public HGGameState getGameState() {
        return gameState;
    }

    public HGSaveData getData() {
        return gameData;
    }

    public HGThread getThread() {
        return gameThread;
    }

    public HGScoreboard getScoreboard() {
        return gameScoreboard;
    }

    public HGWorldborder getWorldborder() {
        return gameWorldborder;
    }

    public HGCommandManager getCommandManager() {
        return commandManager;
    }

    public HGSpectatorManager getSpectatorManager() {
        return spectatorManager;
    }

    public HGGameListener getGameListener() {
        return gameListener;
    }

    public HGIdleStateListener getIdleStateListener() {
        return idleStateListener;
    }

    public World getWorld() {
        return getServer().getWorlds().get(0);
    }

    public World getWorldNether() {
        return getServer().getWorlds().get(1);
    }

    public long getGracePeriod() {
        return grace_period;
    }
}
