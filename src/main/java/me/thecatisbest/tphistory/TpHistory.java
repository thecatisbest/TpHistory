package me.thecatisbest.tphistory;

import me.thecatisbest.tphistory.listeners.TeleportListener;
import me.thecatisbest.tphistory.listeners.PlayerDataListener;
import me.thecatisbest.tphistory.gui.TeleportGUI;
import me.thecatisbest.tphistory.manager.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TpHistory extends JavaPlugin {
    private TeleportManager teleportManager;
    private TeleportGUI teleportGUI;

    @Override
    public void onEnable() {
        // 初始化传送管理器
        teleportManager = new TeleportManager(this);
        teleportGUI = new TeleportGUI(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportGUI(this), this);

        // 注册命令
        getCommand("tpb").setExecutor((sender, cmd, label, args) -> {
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
        // 保存所有数据
        getTeleportManager().shutdown();
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
}
