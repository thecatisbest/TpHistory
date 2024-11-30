package me.thecatisbest.tphistory.gui;

import com.google.common.collect.ImmutableMap;
import me.thecatisbest.tphistory.TpHistory;
import me.thecatisbest.tphistory.gui.holders.TeleportGUIHolder;
import me.thecatisbest.tphistory.manager.TeleportRecord;
import me.thecatisbest.tphistory.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TeleportGUI implements Listener {
    private final TpHistory plugin;
    private final int GUI_SIZE = 45;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public TeleportGUI(TpHistory plugin) {
        this.plugin = plugin;
    }

    public void openTeleportHistory(Player player) {
        Inventory gui = Bukkit.createInventory(new TeleportGUIHolder(), GUI_SIZE, Utils.color("&8近期傳送記錄"));
        List<TeleportRecord> history = plugin.getTeleportManager().getPlayerHistory(player.getUniqueId());
        fillBorder(gui);

        // 检查 history 是否为空或无记录
        if (history == null || history.isEmpty()) {
            player.openInventory(gui);
            addNoneButtons(gui);
            addFunctionButtons(gui);
            return;
        }

        // 定义可用槽位
        List<Integer> availableSlots = Arrays.asList(
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25
        );

        // 遍历历史记录并填充到可用槽位
        int historySize = history.size();
        for (int i = 0; i < availableSlots.size() && i < historySize; i++) {
            TeleportRecord record = history.get(historySize - i - 1);
            gui.setItem(availableSlots.get(i), createTeleportItem(record, i + 1));
        }

        /*
        // 遍历历史记录，最多显示传送记录到 GUI
        for (int i = 10; i < 27 && i - 10 < history.size(); i++) {
            TeleportRecord record = history.get(i - 10); // 从列表的第一个记录开始
            gui.setItem(i, createTeleportItem(record, i - 9));
        }

         */


        addFunctionButtons(gui);
        player.openInventory(gui);
    }

    private void fillBorder(Inventory gui) {
        ItemStack border = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, Utils.color("&7"), null);

        // 填充顶部和底部边框
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
            gui.setItem(GUI_SIZE - 9 + i, border);
        }

        // 填充左右边框
        for (int i = 0; i < GUI_SIZE / 9; i++) {
            gui.setItem(i * 9, border);
            gui.setItem(i * 9 + 8, border);
        }
    }

    private void addFunctionButtons(Inventory gui) {
        // 搜索按钮
        gui.setItem(GUI_SIZE - 5, createGuiItem(
                Material.RED_BED,
                Utils.color("&c關閉"),
                null
                )
        );
    }

    private void addNoneButtons(Inventory gui) {
        // 搜索按钮
        gui.setItem(GUI_SIZE - 23, createGuiItem(
                        Material.BARRIER,
                        Utils.color("&c暫無傳送歷史!"),
                        null
                )
        );
    }

    private ItemStack createTeleportItem(TeleportRecord record, int index) {
        return createGuiItem(
                getMaterialForBiome(record.getBiome()),
                Utils.color("&7#" + index + " &f傳送點"),
                Arrays.asList(
                        Utils.color("&f") + DATE_FORMAT.format(new Date(record.getTime())),
                        "",
                        Utils.color(" &7世界: &f") + record.getWorld(),
                        Utils.color(" &7生態域: &f") + getBiomeDisplayName(record.getBiome()),
                        Utils.color(" &7坐標: &f" + String.format("%d, %d, %d",
                                Math.round(record.getLocation().getX()),
                                Math.round(record.getLocation().getY()),
                                Math.round(record.getLocation().getZ()))),
                        "",
                        Utils.color("&a ▶ &7左鍵&f傳回此處"),
                        Utils.color("&a ▶ &7蹲下+右鍵&f刪除此記錄"
                ))
        );
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (event.getInventory().getHolder() instanceof TeleportGUIHolder) {
            Player player = (Player) event.getWhoClicked();

            if (clickedItem == null) return;

            event.setCancelled(true);

            /*
            int CLEAN_HISTORY = event.getInventory().getSize() - 7;
            if (event.getSlot() == CLEAN_HISTORY) {
                plugin.getTeleportManager().clearHistory(player.getUniqueId());
                player.closeInventory();
                player.sendMessage("&a已清除所有传送历史!");
            }

             */

            int CLOSE = event.getInventory().getSize() - 5;
            if (event.getSlot() == CLOSE) {
                player.closeInventory();
            }

            if (event.getSlot() >= 10 && event.getSlot() <= 27) {
                ClickType clickType = event.getClick();
                int index = event.getSlot() - 10; // 对应的记录索引

                if (clickType == ClickType.LEFT) {
                    plugin.getTeleportManager().teleportToHistory(player, index);
                    player.closeInventory();
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    plugin.getTeleportManager().removeRecord(player.getUniqueId(), index);

                    openTeleportHistory(player); // 刷新界面
                }
            }
        }
    }

    private static final ImmutableMap<Biome, Material> BIOME_MATERIAL = ImmutableMap.<Biome, Material>builder()
            .put(Biome.BADLANDS, Material.DEAD_BUSH)
            .put(Biome.BAMBOO_JUNGLE, Material.BAMBOO)
            .put(Biome.BIRCH_FOREST, Material.BIRCH_SAPLING)
            .put(Biome.CHERRY_GROVE, Material.CHERRY_SAPLING)
            .put(Biome.DARK_FOREST, Material.DARK_OAK_SAPLING)
            .put(Biome.DEEP_DARK, Material.SCULK_SENSOR)
            .put(Biome.DESERT, Material.SAND)
            .put(Biome.DRIPSTONE_CAVES, Material.DRIPSTONE_BLOCK)
            .put(Biome.ERODED_BADLANDS, Material.RED_SAND)
            .put(Biome.FLOWER_FOREST, Material.OXEYE_DAISY)
            .put(Biome.FOREST, Material.OAK_SAPLING)
            .put(Biome.FROZEN_PEAKS, Material.BLUE_ICE)
            .put(Biome.FROZEN_RIVER, Material.ICE)
            .put(Biome.GROVE, Material.SPRUCE_SAPLING)
            .put(Biome.ICE_SPIKES, Material.PACKED_ICE)
            .put(Biome.JAGGED_PEAKS, Material.SNOW_BLOCK)
            .put(Biome.JUNGLE, Material.JUNGLE_SAPLING)
            .put(Biome.LUSH_CAVES, Material.MOSS_BLOCK)
            .put(Biome.MANGROVE_SWAMP, Material.MANGROVE_PROPAGULE)
            .put(Biome.MEADOW, Material.POPPY)
            .put(Biome.MUSHROOM_FIELDS, Material.MYCELIUM)
            .put(Biome.OLD_GROWTH_BIRCH_FOREST, Material.BIRCH_LOG)
            .put(Biome.OLD_GROWTH_PINE_TAIGA, Material.FERN)
            .put(Biome.OLD_GROWTH_SPRUCE_TAIGA, Material.LARGE_FERN)
            .put(Biome.PLAINS, Material.GRASS_BLOCK)
            .put(Biome.RIVER, Material.WATER_BUCKET)
            .put(Biome.SAVANNA, Material.ACACIA_SAPLING)
            .put(Biome.SAVANNA_PLATEAU, Material.ACACIA_LOG)
            .put(Biome.SNOWY_PLAINS, Material.SNOW_BLOCK)
            .put(Biome.SNOWY_SLOPES, Material.POWDER_SNOW_BUCKET)
            .put(Biome.SNOWY_TAIGA, Material.SNOW)
            .put(Biome.SPARSE_JUNGLE, Material.VINE)
            .put(Biome.STONY_PEAKS, Material.POWDER_SNOW_BUCKET)
            .put(Biome.SUNFLOWER_PLAINS, Material.SUNFLOWER)
            .put(Biome.SWAMP, Material.LILY_PAD)
            .put(Biome.TAIGA, Material.SPRUCE_SAPLING)
            .put(Biome.THE_VOID, Material.BEDROCK)
            .put(Biome.WINDSWEPT_FOREST, Material.STONE)
            .put(Biome.WINDSWEPT_GRAVELLY_HILLS, Material.GRAVEL)
            .put(Biome.WINDSWEPT_HILLS, Material.STONE)
            .put(Biome.WINDSWEPT_SAVANNA, Material.ACACIA_SAPLING)
            .put(Biome.WOODED_BADLANDS, Material.DARK_OAK_SAPLING)

            // 地獄生態域
            .put(Biome.NETHER_WASTES, Material.NETHERRACK)
            .put(Biome.SOUL_SAND_VALLEY, Material.SOUL_SAND)
            .put(Biome.CRIMSON_FOREST, Material.CRIMSON_FUNGUS)
            .put(Biome.WARPED_FOREST, Material.WARPED_FUNGUS)
            .put(Biome.BASALT_DELTAS, Material.POLISHED_BASALT)

            // 終界生態域
            .put(Biome.THE_END, Material.END_STONE)
            .put(Biome.SMALL_END_ISLANDS, Material.END_STONE_BRICKS)
            .put(Biome.END_BARRENS, Material.PURPUR_PILLAR)
            .put(Biome.END_HIGHLANDS, Material.PURPUR_BLOCK)
            .put(Biome.END_MIDLANDS, Material.CHORUS_FLOWER)

            // 海岸生態域
            .put(Biome.BEACH, Material.SANDSTONE)
            .put(Biome.SNOWY_BEACH, Material.SNOW)
            .put(Biome.STONY_SHORE, Material.STONE)

            // 海洋生態域
            .put(Biome.OCEAN, Material.WATER_BUCKET)
            .put(Biome.WARM_OCEAN, Material.TUBE_CORAL)
            .put(Biome.COLD_OCEAN, Material.COD)
            .put(Biome.DEEP_OCEAN, Material.PRISMARINE_CRYSTALS)
            .put(Biome.DEEP_COLD_OCEAN, Material.SALMON)
            .put(Biome.DEEP_FROZEN_OCEAN, Material.PACKED_ICE)
            .put(Biome.DEEP_LUKEWARM_OCEAN, Material.PUFFERFISH)
            .put(Biome.LUKEWARM_OCEAN, Material.TROPICAL_FISH)
            .put(Biome.FROZEN_OCEAN, Material.ICE)

            .build();

    private static final ImmutableMap<Biome, String> BIOME_DISPLAY_NAMES = ImmutableMap.<Biome, String>builder()
            // 平地生態域
            .put(Biome.BADLANDS, "惡地")
            .put(Biome.BAMBOO_JUNGLE, "竹林")
            .put(Biome.BIRCH_FOREST, "樺木森林")
            .put(Biome.CHERRY_GROVE, "櫻花樹林")
            .put(Biome.DARK_FOREST, "黑森林")
            .put(Biome.DEEP_DARK, "深淵")
            .put(Biome.DESERT, "沙漠")
            .put(Biome.DRIPSTONE_CAVES, "鐘乳石洞窟")
            .put(Biome.ERODED_BADLANDS, "侵蝕惡地")
            .put(Biome.FLOWER_FOREST, "繁花森林")
            .put(Biome.FOREST, "森林")
            .put(Biome.FROZEN_PEAKS, "霜凍山峰")
            .put(Biome.FROZEN_RIVER, "寒凍河流")
            .put(Biome.GROVE, "雪林")
            .put(Biome.ICE_SPIKES, "冰刺")
            .put(Biome.JAGGED_PEAKS, "尖峭山峰")
            .put(Biome.JUNGLE, "叢林")
            .put(Biome.LUSH_CAVES, "蒼鬱洞窟")
            .put(Biome.MANGROVE_SWAMP, "紅樹林沼澤")
            .put(Biome.MEADOW, "草甸")
            .put(Biome.MUSHROOM_FIELDS, "蘑菇地")
            .put(Biome.OLD_GROWTH_BIRCH_FOREST, "原始樺木森林")
            .put(Biome.OLD_GROWTH_PINE_TAIGA, "原生松木針葉林")
            .put(Biome.OLD_GROWTH_SPRUCE_TAIGA, "原生杉木針葉林")
            .put(Biome.PLAINS, "平原")
            .put(Biome.RIVER, "河流")
            .put(Biome.SAVANNA, "莽原")
            .put(Biome.SAVANNA_PLATEAU, "莽原高地")
            .put(Biome.SNOWY_PLAINS, "雪原")
            .put(Biome.SNOWY_SLOPES, "雪坡")
            .put(Biome.SNOWY_TAIGA, "冰雪針葉林")
            .put(Biome.SPARSE_JUNGLE, "稀疏叢林")
            .put(Biome.STONY_PEAKS, "裸岩山峰")
            .put(Biome.SUNFLOWER_PLAINS, "向日葵平原")
            .put(Biome.SWAMP, "沼澤")
            .put(Biome.TAIGA, "針葉林")
            .put(Biome.THE_VOID, "虛空")
            .put(Biome.WINDSWEPT_FOREST, "風蝕森林")
            .put(Biome.WINDSWEPT_GRAVELLY_HILLS, "風蝕礫質丘陵")
            .put(Biome.WINDSWEPT_HILLS, "風蝕丘陵")
            .put(Biome.WINDSWEPT_SAVANNA, "風蝕莽原")
            .put(Biome.WOODED_BADLANDS, "疏林惡地")

            // 地獄生態域
            .put(Biome.NETHER_WASTES, "地獄荒原")
            .put(Biome.SOUL_SAND_VALLEY, "靈魂砂谷")
            .put(Biome.CRIMSON_FOREST, "緋紅森林")
            .put(Biome.WARPED_FOREST, "扭曲森林")
            .put(Biome.BASALT_DELTAS, "玄武岩三角洲")

            // 終界生態域
            .put(Biome.THE_END, "終界")
            .put(Biome.SMALL_END_ISLANDS, "終界小島")
            .put(Biome.END_BARRENS, "終界荒地")
            .put(Biome.END_HIGHLANDS, "終界高地")
            .put(Biome.END_MIDLANDS, "終界平地")

            // 海岸生態域
            .put(Biome.BEACH, "沙灘")
            .put(Biome.SNOWY_BEACH, "冰雪沙灘")
            .put(Biome.STONY_SHORE, "石岸")

            // 海洋生態域
            .put(Biome.OCEAN, "海洋")
            .put(Biome.WARM_OCEAN, "溫暖海洋")
            .put(Biome.COLD_OCEAN, "寒冷海洋")
            .put(Biome.DEEP_OCEAN, "深海")
            .put(Biome.DEEP_COLD_OCEAN, "寒冷深海")
            .put(Biome.DEEP_FROZEN_OCEAN, "冰凍深海")
            .put(Biome.DEEP_LUKEWARM_OCEAN, "溫和深海")
            .put(Biome.LUKEWARM_OCEAN, "溫和海洋")
            .put(Biome.FROZEN_OCEAN, "寒凍海洋")

            .build();

    private Material getMaterialForBiome(Biome biome) {
        return BIOME_MATERIAL.getOrDefault(biome, Material.COMPASS);
    }

    private String getBiomeDisplayName(Biome biome) {
        return BIOME_DISPLAY_NAMES.getOrDefault(biome, "未知");
    }
}
