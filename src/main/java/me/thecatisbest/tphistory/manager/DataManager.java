package me.thecatisbest.tphistory.manager;

import me.thecatisbest.tphistory.TpHistory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final TpHistory plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    public DataManager(TpHistory plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "history.yml");
        createFile();
    }

    private void createFile() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            plugin.saveResource("history.yml", false);
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveHistory(UUID playerId, List<TeleportRecord> records) {
        String playerPath = playerId.toString();
        dataConfig.set(playerPath, null); // 清除现有数据

        ConfigurationSection playerSection = dataConfig.createSection(playerPath);

        for (int i = 0; i < records.size(); i++) {
            TeleportRecord record = records.get(i);
            ConfigurationSection recordSection = playerSection.createSection(String.valueOf(i));

            // 保存位置信息
            Location loc = record.getLocation();
            recordSection.set("world", loc.getWorld().getName());
            recordSection.set("x", loc.getX());
            recordSection.set("y", loc.getY());
            recordSection.set("z", loc.getZ());
            recordSection.set("yaw", loc.getYaw());
            recordSection.set("pitch", loc.getPitch());

            // 保存其他数据
            recordSection.set("time", record.getTime());
            recordSection.set("biome", record.getBiome().toString());
        }

        saveConfig();
    }

    public List<TeleportRecord> loadHistory(UUID playerId) {
        List<TeleportRecord> records = new ArrayList<>();
        ConfigurationSection playerSection = dataConfig.getConfigurationSection(playerId.toString());

        if (playerSection == null) {
            return records;
        }

        for (String key : playerSection.getKeys(false)) {
            ConfigurationSection recordSection = playerSection.getConfigurationSection(key);
            if (recordSection == null) continue;

            try {
                // 重建Location对象
                String worldName = recordSection.getString("world");
                double x = recordSection.getDouble("x");
                double y = recordSection.getDouble("y");
                double z = recordSection.getDouble("z");
                float yaw = (float) recordSection.getDouble("yaw");
                float pitch = (float) recordSection.getDouble("pitch");

                if (Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("World '" + worldName + "' not found, skipping record");
                    continue;
                }

                Location location = new Location(
                        Bukkit.getWorld(worldName),
                        x, y, z, yaw, pitch
                );

                // 获取其他数据
                long time = recordSection.getLong("time");
                Biome biome = Biome.valueOf(recordSection.getString("biome"));

                records.add(new TeleportRecord(location, time, biome, worldName));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Error loading teleport record for player " + playerId, e);
            }
        }

        return records;
    }

    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save history.yml", e);
        }
    }

    public void deletePlayerData(UUID playerId) {
        dataConfig.set(playerId.toString(), null);
        saveConfig();
    }
}
