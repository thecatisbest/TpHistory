package me.thecatisbest.tphistory.listeners;

import me.thecatisbest.tphistory.TpHistory;
import me.thecatisbest.tphistory.manager.TeleportRecord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.EnumSet;
import java.util.Set;

public class TeleportListener implements Listener {
    private final TpHistory plugin;

    public TeleportListener(TpHistory plugin) {
        this.plugin = plugin;
    }

    private static final Set<PlayerTeleportEvent.TeleportCause> IGNORED_CAUSES = EnumSet.of(
            PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT,
            PlayerTeleportEvent.TeleportCause.DISMOUNT,
            PlayerTeleportEvent.TeleportCause.END_PORTAL,
            PlayerTeleportEvent.TeleportCause.END_GATEWAY,
            PlayerTeleportEvent.TeleportCause.ENDER_PEARL,
            PlayerTeleportEvent.TeleportCause.EXIT_BED,
            PlayerTeleportEvent.TeleportCause.NETHER_PORTAL,
            PlayerTeleportEvent.TeleportCause.SPECTATE
    );

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (IGNORED_CAUSES.contains(event.getCause())) {
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (isSameLocation(from, to)) {
            MiniMessage miniMessage = MiniMessage.miniMessage();

            Component message1 = miniMessage.deserialize(" <#afdcff>系統</#afdcff><white> ⋅ <gray>你進行了傳送，但是此位置已在記錄中，")
                    .clickEvent(ClickEvent.runCommand("/backui"))
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<gray>點擊開啟傳送記錄")));

            Component message2 = miniMessage.deserialize(" <#afdcff>系統</#afdcff><white> ⋅ <gray>因此不會重複記錄，點此查看<#e6bbf6>近期傳送記錄<gray>。")
                    .clickEvent(ClickEvent.runCommand("/backui"))
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<gray>點擊開啟傳送記錄")));

            player.sendMessage(message1);
            player.sendMessage(message2);
            return;
        }

        TeleportRecord record = new TeleportRecord(
                from,
                System.currentTimeMillis(),
                from.getWorld().getBiome(
                        from.getBlockX(),
                        from.getBlockY(),
                        from.getBlockZ()
                ),
                from.getWorld().getName()
        );

        if (!plugin.getTeleportManager().isLocationExists(player.getUniqueId(), record)) {
            plugin.getTeleportManager().addTeleportRecord(player, record);

            MiniMessage miniMessage = MiniMessage.miniMessage();
            Component message = miniMessage.deserialize(" <#afdcff>系統</#afdcff><white> ⋅ <gray>已記錄傳送前的位置，點此查看<#e6bbf6>近期傳送記錄<gray>。")
                    .clickEvent(ClickEvent.runCommand("/backui"))
                    .hoverEvent(HoverEvent.showText(miniMessage.deserialize("<gray>點擊開啟傳送記錄")));

            player.sendMessage(message);
        }
    }

    private boolean isSameLocation(Location from, Location to) {
        if (from.getWorld() != to.getWorld()) return false;

        return from.getBlockX() == to.getBlockX() &&
                from.getBlockY() == to.getBlockY() &&
                from.getBlockZ() == to.getBlockZ();
    }
}