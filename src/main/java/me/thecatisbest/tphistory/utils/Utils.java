package me.thecatisbest.tphistory.utils;


import me.thecatisbest.tphistory.TpHistory;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern pattern = Pattern.compile("#[a-fA-F\\d]{6}");
    private final TpHistory plugin;
    private static boolean isFolia;

    public Utils(TpHistory plugin) {
        this.plugin = plugin;
        isFolia = checkFolia();
    }

    private static boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static String color(String text) {
        Matcher match = pattern.matcher(text);
        while (match.find()) {
            String color = text.substring(match.start(), match.end());
            text = text.replace(color, ChatColor.of(color) + "");
            match = pattern.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> color(List<String> msg){
        final List<String> colored = new ArrayList<>();
        for (String s : msg) {
            colored.add(color(s));
        }
        return colored;
    }

    public void runTask(Runnable task) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public void runDelayedTask(Runnable task, long delay) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
        }
    }

    public void runGlobalTask(Runnable task) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }

    public void runAsyncTask(Runnable task) {
        if (isFolia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }

    public void cancelTasks() {
        if (isFolia) {
            // Folia will handle task cancellation automatically when plugin is disabled
        } else {
            plugin.getServer().getScheduler().cancelTasks(plugin);
        }
    }
}
