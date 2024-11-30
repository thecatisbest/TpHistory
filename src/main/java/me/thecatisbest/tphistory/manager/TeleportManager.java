package me.thecatisbest.tphistory.manager;

import me.thecatisbest.tphistory.TpHistory;
import me.thecatisbest.tphistory.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.*;

public class TeleportManager {
    private final TpHistory plugin;
    private final Map<UUID, List<TeleportRecord>> teleportHistory;
    private final DataManager dataManager;
    private final int MAX_HISTORY = 10;

    public TeleportManager(TpHistory plugin) {
        this.plugin = plugin;
        this.teleportHistory = new HashMap<>();
        this.dataManager = new DataManager(plugin);

        // 在服务器启动时加载所有在线玩家的数据
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                loadPlayerData(player.getUniqueId());
            }
        }, 20L);
    }

    public void loadPlayerData(UUID playerId) {
        List<TeleportRecord> records = dataManager.loadHistory(playerId);
        teleportHistory.put(playerId, records);
    }

    public void savePlayerData(UUID playerId) {
        List<TeleportRecord> records = teleportHistory.get(playerId);
        if (records != null) {
            dataManager.saveHistory(playerId, records);
        }
    }

    public void addTeleportRecord(Player player, TeleportRecord record) {
        UUID playerId = player.getUniqueId();
        teleportHistory.putIfAbsent(playerId, new ArrayList<>());
        List<TeleportRecord> playerHistory = teleportHistory.get(playerId);

        if (playerHistory.size() >= MAX_HISTORY) {
            playerHistory.remove(0);
        }

        playerHistory.add(record);

        // 异步保存数据
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            savePlayerData(playerId);
        });
    }

    // 檢查是否存在相同位置的記錄
    public boolean isLocationExists(UUID playerId, TeleportRecord newRecord) {
        List<TeleportRecord> playerHistory = teleportHistory.get(playerId);
        if (playerHistory == null) return false;

        Location to = newRecord.getLocation();
        for (TeleportRecord record : playerHistory) {
            Location from = record.getLocation();

            if (from.getWorld().getName().equals(to.getWorld().getName()) &&
                    from.getBlockX() == to.getBlockX() &&
                    from.getBlockY() == to.getBlockY() &&
                    from.getBlockZ() == to.getBlockZ()) return true;
        }
        return false;
    }

    public List<TeleportRecord> getPlayerHistory(UUID playerId) {
        return teleportHistory.getOrDefault(playerId, new ArrayList<>());
    }

    // 删除特定的传送记录
    public void removeRecord(UUID playerId, int index) {
        List<TeleportRecord> history = teleportHistory.get(playerId);
        if (history != null && index >= 0 && index < history.size()) {
            history.remove(index);
        }
    }

    public void teleportToHistory(Player player, int index) {
        List<TeleportRecord> history = teleportHistory.get(player.getUniqueId());

        if (history == null || index >= history.size()) {
            return;
        }

        TeleportRecord record = history.get(index);
        player.teleport(record.getLocation());
        player.sendMessage(Utils.color(" #fb0867系統 &7| #ccfcbe已成功&7傳送到選擇的位置！"));
    }

    public void clearHistory(UUID playerId) {
        teleportHistory.remove(playerId);
        dataManager.deletePlayerData(playerId);
    }

    public void shutdown() {
        // 服务器关闭时保存所有数据
        for (UUID playerId : teleportHistory.keySet()) {
            savePlayerData(playerId);
        }
    }
}

