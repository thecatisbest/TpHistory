package me.thecatisbest.tphistory.listeners;

import me.thecatisbest.tphistory.TpHistory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FoliaTeleportListener implements Listener {

    private final TpHistory plugin;

    public static ConcurrentHashMap<UUID, Location> lastTickLocations = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Long> teleportFlags = new ConcurrentHashMap<>();
    public static long TELEPORT_IGNORE_THRESHOLD = 10000L;

    public FoliaTeleportListener(TpHistory plugin) {
        this.plugin = plugin;
    }

    public static boolean hasMovementAffectingPotion(Player player) {
        return player.hasPotionEffect(PotionEffectType.SPEED) || player.hasPotionEffect(PotionEffectType.LEVITATION);
    }

    // 判斷玩家是否在近期因傳送而觸發大幅位移
    public static boolean teleportRecently(UUID uuid, long currentTime) {
        if (teleportFlags.containsKey(uuid)) {
            long teleportTime = teleportFlags.get(uuid);
            if (currentTime - teleportTime < TELEPORT_IGNORE_THRESHOLD) {
                return true;
            } else {
                teleportFlags.remove(uuid);
            }
        }
        return false;
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof EnderPearl) {
            Object shooter = event.getEntity().getShooter();
            if (shooter instanceof Player player) {
                teleportFlags.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.CHORUS_FRUIT) {
            Player player = event.getPlayer();
            teleportFlags.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public static boolean isSameLocation(Location from, Location to) {
        if (from.getWorld() != to.getWorld()) return false;

        return from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ();
    }
}
