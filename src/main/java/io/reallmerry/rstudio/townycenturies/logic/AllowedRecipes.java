package io.reallmerry.rstudio.townycenturies.logic;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AllowedRecipes {

    private final Map<Material, Age> minAgeForMaterial = new EnumMap<>(Material.class);

    private final TownyCenturies plugin;

    public AllowedRecipes(TownyCenturies plugin) {
        this.plugin = plugin;
    }

    public void load() {
    ConfigurationSection root = plugin.getConfig().getConfigurationSection("overrides");
    }

    public void buildFromDefaultsAndConfig() {
        minAgeForMaterial.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("overrides");
        if (root == null) {
            return;
        }

        allowFrom(Age.PRIMITIVE, Material.CRAFTING_TABLE, Material.FURNACE, Material.CHEST, Material.CAMPFIRE);
        allowFrom(Age.PRIMITIVE, Material.WOODEN_SWORD, Material.WOODEN_PICKAXE, Material.WOODEN_AXE, Material.WOODEN_SHOVEL, Material.WOODEN_HOE);
        allowFrom(Age.PRIMITIVE, Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);

        allowFrom(Age.STONE, Material.BARREL, Material.LEATHER_HORSE_ARMOR, Material.LEAD, Material.SADDLE, Material.FISHING_ROD);
        allowFrom(Age.STONE,
                Material.OAK_BOAT, Material.SPRUCE_BOAT, Material.BIRCH_BOAT, Material.JUNGLE_BOAT, Material.ACACIA_BOAT, Material.DARK_OAK_BOAT);
        allowFrom(Age.STONE, Material.STONE_SWORD, Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SHOVEL, Material.STONE_HOE);

        allowFrom(Age.IRON,
                Material.IRON_SWORD, Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SHOVEL, Material.IRON_HOE,
                Material.GOLDEN_SWORD, Material.GOLDEN_PICKAXE, Material.GOLDEN_AXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_HOE,
                Material.GOLD_BLOCK, Material.IRON_BLOCK,
                Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,

                Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,

                Material.GOLDEN_HORSE_ARMOR, Material.IRON_HORSE_ARMOR,
                Material.FLETCHING_TABLE, Material.BOW, Material.COMPOSTER,
                Material.SHIELD, Material.ITEM_FRAME, Material.BOOK, Material.LECTERN, Material.BOOKSHELF, Material.WRITABLE_BOOK, Material.TURTLE_HELMET
        );

        allowFrom(Age.EARLY_MEDIEVAL,
                Material.DIAMOND_BLOCK,
                Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
                Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE,
                Material.DIAMOND_HORSE_ARMOR, Material.BELL, Material.IRON_TRAPDOOR, Material.IRON_DOOR, Material.COMPASS,
                Material.CARTOGRAPHY_TABLE, Material.MAP, Material.PAPER, Material.CLOCK, Material.TARGET, Material.CHAIN,
                Material.IRON_BARS, Material.HEAVY_WEIGHTED_PRESSURE_PLATE, Material.CROSSBOW, Material.SHEARS, Material.ANVIL,
                Material.GRINDSTONE, Material.CAULDRON, Material.STONECUTTER, Material.ARMOR_STAND, Material.LANTERN, Material.SOUL_LANTERN,
                Material.PAINTING, Material.FLOWER_POT
        );

        allowFrom(Age.CLASSIC_MEDIEVAL,
                Material.HOPPER, Material.DISPENSER, Material.FERMENTED_SPIDER_EYE, Material.BLAZE_POWDER, Material.BREWING_STAND,
                Material.LOOM, Material.GLISTERING_MELON_SLICE, Material.GOLDEN_APPLE, Material.GOLDEN_CARROT, Material.ENCHANTING_TABLE,
                Material.ENDER_CHEST, Material.RESPAWN_ANCHOR, Material.END_CRYSTAL, Material.REDSTONE_LAMP, Material.LODESTONE,
                Material.BLAST_FURNACE, Material.SMOKER, Material.NOTE_BLOCK, Material.JUKEBOX
        );

        allowFrom(Age.LATE_MEDIEVAL,
                Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
                Material.NETHERITE_SWORD, Material.NETHERITE_PICKAXE, Material.NETHERITE_AXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_HOE,
                Material.NETHERITE_BLOCK, Material.SMITHING_TABLE, Material.DAYLIGHT_DETECTOR, Material.COMPARATOR, Material.REPEATER,
                Material.PISTON, Material.STICKY_PISTON, Material.OBSERVER, Material.MINECART, Material.CHEST_MINECART, Material.HOPPER_MINECART,
                Material.TNT_MINECART, Material.FURNACE_MINECART,
                Material.RAIL, Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.ACTIVATOR_RAIL, Material.TNT
        );
        applyOverridesFromConfig();
    }


    // код который сами просили
    private void applyOverridesFromConfig() {
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("overrides");
        if (root == null) return;
        for (String ageKey : root.getKeys(false)) {
            Age age;
            try { age = Age.fromName(ageKey); } catch (Exception e) { continue; }
            ConfigurationSection sec = root.getConfigurationSection(ageKey);
            if (sec == null) continue;
            for (String matName : sec.getStringList("allow")) {
                try {
                    Material m = Material.valueOf(matName.toUpperCase());
                    allowFrom(age, m);
                } catch (Exception ignored) {}
            }
        }
    }

    @SafeVarargs
    private final void allowFrom(Age age, Material... materials) {
        for (Material m : materials) {
            minAgeForMaterial.merge(m, age, (oldV, newV) -> oldV.ordinal() <= newV.ordinal() ? oldV : newV);
        }
    }

    public Age requiredAge(Material material) {
        return minAgeForMaterial.get(material);
    }

    public boolean isAllowed(Material material, Age current, PolicyMode policy) {
        Age req = requiredAge(material);
        if (policy == PolicyMode.GATED_ONLY) {
            if (req == null) return true;
            return current.ordinal() >= req.ordinal();
        } else {
            if (req == null) return false;
            return current.ordinal() >= req.ordinal();
        }
    }
    public Set<Material> allGated() {
        return EnumSet.copyOf(minAgeForMaterial.keySet());
    }
}
