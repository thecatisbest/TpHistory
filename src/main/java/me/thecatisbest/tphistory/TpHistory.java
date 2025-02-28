package me.thecatisbest.tphistory;

import me.thecatisbest.tphistory.listeners.FoliaTeleportListener;
import me.thecatisbest.tphistory.listeners.TeleportListener;
import me.thecatisbest.tphistory.listeners.PlayerDataListener;
import me.thecatisbest.tphistory.gui.TeleportGUI;
import me.thecatisbest.tphistory.manager.TeleportManager;
import me.thecatisbest.tphistory.manager.TeleportRecord;
import me.thecatisbest.tphistory.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.thecatisbest.tphistory.listeners.FoliaTeleportListener.*;

public class TpHistory extends JavaPlugin {
    private TeleportManager teleportManager;
    private TeleportGUI teleportGUI;
    private Utils utils;

    @Override
    public void onEnable() {
        // 初始化传送管理器
        utils = new Utils(this);
        teleportManager = new TeleportManager(this);
        teleportGUI = new TeleportGUI(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportGUI(this), this);

        // 注册命令
        getCommand("backui").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                teleportGUI.openTeleportHistory(player);
                return true;
            }
            return false;
        });

        if (Utils.isFolia()) {
            getServer().getPluginManager().registerEvents(new FoliaTeleportListener(this), this);
            getServer().getAsyncScheduler().runAtFixedRate(this, task -> {
                long currentTime = System.currentTimeMillis();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    Location currentLocation = player.getLocation();

                    if (player.isGliding() || hasMovementAffectingPotion(player) || teleportRecently(uuid, currentTime)
                            || player.isFlying() || player.getGameMode() == GameMode.SPECTATOR || player.getVehicle() != null) {
                        // 更新記錄（因這些情況不參與偵測）
                        lastTickLocations.put(uuid, currentLocation);
                        continue;
                    }

                    // 如果之前有記錄，則偵測本 tick 與上一次記錄的位移
                    if (lastTickLocations.containsKey(uuid)) {
                        Location previous = lastTickLocations.get(uuid);
                        if (previous != null && previous.getWorld() != null && currentLocation.getWorld() != null
                                && previous.getWorld().equals(currentLocation.getWorld())) {
                            double tickDistance = previous.distance(currentLocation);
                            if (tickDistance > 8) {
                                double dx = currentLocation.getX() - previous.getX();
                                double dz = currentLocation.getZ() - previous.getZ();
                                double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                                double verticalDifference = Math.abs(currentLocation.getY() - previous.getY());
                                // 如果主要是垂直位移（水平位移很小），視為高空落下，此時不更新記錄
                                if (horizontalDistance < 1.0 && verticalDifference > 5) {
                                    lastTickLocations.put(uuid, currentLocation);
                                    continue;
                                } else {
                                    TeleportRecord record = new TeleportRecord(
                                            previous,
                                            System.currentTimeMillis(),
                                            previous.getWorld().getBiome(
                                                    previous.getBlockX(),
                                                    previous.getBlockY(),
                                                    previous.getBlockZ()
                                            ),
                                            previous.getWorld().getName()
                                    );

                                    // 檢查是否已存在相同位置的記錄
                                    if (!getTeleportManager().isLocationExists(player.getUniqueId(), record)) {
                                        getTeleportManager().addTeleportRecord(player, record);

                                        MiniMessage miniMessage = MiniMessage.miniMessage();
                                        Component message = miniMessage.deserialize(" <#afdcff>系統</#afdcff><white> ⋅ <gray>已記錄傳送前的位置，點此查看<#e6bbf6>近期傳送記錄<gray>。")
                                                .clickEvent(ClickEvent.runCommand("/backui"))
                                                .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<gray>點擊開啟傳送記錄")));

                                        player.sendMessage(message);
                                    }
                                }
                            }
                        }
                    }
                    // 更新記錄（僅在非落體情況更新）
                    lastTickLocations.put(uuid, currentLocation);
                }
            }, 50L, 50L, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onDisable() {
        // 保存所有数据
        getTeleportManager().shutdown();
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
    public TeleportGUI getTeleportGUI() {
        return teleportGUI;
    }
    public Utils getUtils() {
        return utils;
    }
}
