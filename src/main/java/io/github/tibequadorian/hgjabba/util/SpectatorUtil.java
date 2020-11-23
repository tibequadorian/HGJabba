package io.github.tibequadorian.hgjabba.util;

import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_16_R3.PacketPlayOutCamera;

public class SpectatorUtil {
	
	public static void setCamera(Player spectator, Entity entity) {
		spectator.teleport(entity);
		PacketPlayOutCamera camera = new PacketPlayOutCamera(((CraftEntity) entity).getHandle());
		((CraftPlayer) spectator).getHandle().playerConnection.sendPacket(camera);
	}

	public static void resetCamera(Player spectator) {
		setCamera(spectator, (Entity) spectator);
	}
}
