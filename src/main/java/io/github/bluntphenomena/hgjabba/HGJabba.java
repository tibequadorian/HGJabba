package io.github.bluntphenomena.hgjabba;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.bluntphenomena.hgjabba.listener.HGGameListener;
import io.github.bluntphenomena.hgjabba.listener.HGIdleStateListener;
import io.github.bluntphenomena.hgjabba.savedata.HGPlayerData;
import io.github.bluntphenomena.hgjabba.savedata.HGSaveData;
import io.github.bluntphenomena.hgjabba.util.WorldCreatorUtil;

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

    private static final String WORLD_NAME = "world-hgjabba";
    private World world;
    private World world_nether;
    private Location spawn;
    private long grace_period = DEFAULT_GRACE_PERIOD;

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

        // setup
        if (!gameData.isSetup()) {
            setupGame();
        }
        // worlds
        world = Bukkit.createWorld(new WorldCreator(WORLD_NAME).environment(Environment.NORMAL));
        world_nether = Bukkit.createWorld(new WorldCreator(WORLD_NAME+"_nether").environment(Environment.NETHER));
        spawn = new Location(world, 0, 125, 0);
        // run
        if (gameData.isRunning()) {
            setWaitingState();
        }

    }

    @Override
    public void onDisable() {

        gameData.saveData();
    }


    private void setupGame() {
        WorldCreatorUtil.initialize(WORLD_NAME, 1000);
        world = WorldCreatorUtil.create();
        // create safespace area
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
        world.setGameRuleValue("doDaylightCycle", "false");
        gameData.setSetup(true);
    }

    public void runGame() {
        setWaitingState();
        HGChat.broadcastPrefix(ChatColor.GOLD+"The game will begin when all players are connected...");
        gameData.setRunning(true);
    }

    private void startGame() {
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
        gameData.setStarted(true);
    }


    private boolean allPlayersOnline() {
        for (OfflinePlayer p : gameData.getAlivePlayers()) {
            if (!p.isOnline())
                return false;
        }
        return true;
    }

    public void setWaitingState() {
        world.setGameRuleValue("doDaylightCycle", "false");
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
        world.setGameRuleValue("doDaylightCycle", "true");
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
        return world;
    }

    public World getWorldNether() {
        return world_nether;
    }

    public Location getSpawn() {
        return spawn;
    }

    public long getGracePeriod() {
        return grace_period;
    }
}
