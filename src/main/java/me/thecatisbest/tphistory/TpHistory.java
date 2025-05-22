package me.thecatisbest.tphistory;

import me.thecatisbest.tphistory.listeners.TeleportListener;
import me.thecatisbest.tphistory.listeners.PlayerDataListener;
import me.thecatisbest.tphistory.gui.TeleportGUI;
import me.thecatisbest.tphistory.manager.TeleportManager;
import me.thecatisbest.tphistory.utils.Utils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TpHistory extends JavaPlugin {
    private TeleportManager teleportManager;
    private TeleportGUI teleportGUI;
    private Utils utils;

    @Override
    public void onEnable() {
        utils = new Utils(this);
        teleportManager = new TeleportManager(this);
        teleportGUI = new TeleportGUI(this);

        getLogger().info("插件加載成功!");
        getLogger().info("以下資訊是由貴服强制要求標注:");
        getLogger().info("此功能的靈感來源于 輝煌伺服器");
        getLogger().info("原項目連結:");
        getLogger().info("https://github.com/BrilliantTeam/TpHistory");

        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportGUI(this), this);

        getCommand("backui").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                teleportGUI.openTeleportHistory(player);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onDisable() {
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
