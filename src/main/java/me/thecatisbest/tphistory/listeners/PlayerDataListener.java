package me.thecatisbest.tphistory.listeners;

import me.thecatisbest.tphistory.TpHistory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerDataListener implements Listener {
    private final TpHistory plugin;

    public PlayerDataListener(TpHistory plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 玩家加入时加载数据
        plugin.getUtils().runAsyncTask(() -> {
            plugin.getTeleportManager().loadPlayerData(player.getUniqueId());
            player.sendMessage("happy");
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // 玩家退出时保存数据
        UUID playerId = event.getPlayer().getUniqueId();
        plugin.getTeleportManager().savePlayerData(playerId);
        event.getPlayer().sendMessage("happy");
    }
}