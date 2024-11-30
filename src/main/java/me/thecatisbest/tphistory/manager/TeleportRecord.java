package me.thecatisbest.tphistory.manager;

import org.bukkit.Location;
import org.bukkit.block.Biome;

public class TeleportRecord {
    private final Location location;
    private final long time;
    private final Biome biome;
    private final String world;

    public TeleportRecord(Location location, long time, Biome biome, String world) {
        this.location = location;
        this.time = time;
        this.biome = biome;
        this.world = world;
    }

    public Location getLocation() {
        return location;
    }

    public long getTime() {
        return time;
    }

    public Biome getBiome() {
        return biome;
    }

    public String getWorld() {
        return world;
    }
}
