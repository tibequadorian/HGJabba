package io.github.bluntphenomena.hgjabba.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;

public class WorldCreatorUtil {
	
	private static String world_name;
	private static int radius;
	
	
	public static void initialize(String world_name, int radius) {
		WorldCreatorUtil.world_name = world_name;
		WorldCreatorUtil.radius = radius;
	}
	
	
	public static World create() {
		
		WorldCreator worldCreator = null;
		World wld = null;
		
		boolean check = false;
		while (!check) {
			worldCreator = new WorldCreator(world_name);
			wld = Bukkit.createWorld(worldCreator.environment(Environment.NORMAL));
			Bukkit.getLogger().info("Checking world...");
			check = checkWorld(wld);
			if (!check) {
				Bukkit.unloadWorld(world_name, false);
				try {
					FileUtils.deleteDirectory(wld.getWorldFolder());
				} catch (IOException e) { e.printStackTrace(); }
			}
		}
		Bukkit.getLogger().info("World is suitable!");
		
		worldCreator = new WorldCreator(world_name+"_nether");
		Bukkit.createWorld(worldCreator.environment(Environment.NETHER));
		
		return wld;
	}
	
	private static HashMap<Biome, Double> getBiomeFrequency(World world) {
		HashMap<Biome,Integer> baf = new HashMap<Biome,Integer>(); // biome absolute frequency
		HashMap<Biome,Double> brf = new HashMap<Biome,Double>(); // biome relative frequency
		for (Biome b : Biome.values()) {
			baf.put(b, 0);
		}
		Biome biome;
		for (int x = -radius; x < radius; x++) {
			for (int z = -radius; z < radius; z++) {
				biome = world.getBiome(x, z);
				baf.put(biome, baf.get(biome)+1);
			}
		}
		double area = (double)(4*radius*radius);
		for (Biome b : baf.keySet()) {
			brf.put(b, baf.get(b) / area);
		}
		
		return brf;
	}
	
	private static boolean checkWorld(World world) {
		
		HashMap<Biome,Double> biomeFreq = getBiomeFrequency(world);
		DecimalFormat df = new DecimalFormat("##.##");
		
		double oceanFreq = biomeFreq.get(Biome.OCEAN) + biomeFreq.get(Biome.DEEP_OCEAN) + biomeFreq.get(Biome.FROZEN_OCEAN);
		if (oceanFreq > 0.125) {
			Bukkit.getLogger().info("More than 12.5 % ocean ("+df.format(oceanFreq*100)+" %), world gets discarded!");
			return false;
		}
		
		double plainsFreq = biomeFreq.get(Biome.PLAINS);
		if (plainsFreq < 0.125) {
			Bukkit.getLogger().info("Less than 12.5 % plains ("+df.format(plainsFreq*100)+" %), world gets discarded!");
			return false;
		}
		
		return true;
	}
}
