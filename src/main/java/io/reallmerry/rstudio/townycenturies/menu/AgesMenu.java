package io.reallmerry.rstudio.townycenturies.menu;

import com.palmergames.bukkit.towny.object.Town;
import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.config.Messages;
import io.reallmerry.rstudio.townycenturies.data.TownAgeManager;
import io.reallmerry.rstudio.townycenturies.hook.TownyHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AgesMenu {

    private final Plugin plugin;
    private final TownAgeManager ageManager;
    private final TownyHook towny;
    private final Messages messages;
    private final ConfigurationSection root;

    private final NamespacedKey KEY_AGE;
    private final NamespacedKey KEY_PRICE;
    private final NamespacedKey KEY_RES;

    public AgesMenu(Plugin plugin,
                    TownAgeManager ageManager,
                    TownyHook towny,
                    Messages messages,
                    ConfigurationSection root) {
        this.plugin = plugin;
        this.ageManager = ageManager;
        this.towny = towny;
        this.messages = messages;
        this.root = root;

        this.KEY_AGE = new NamespacedKey(plugin, "tc_age");
        this.KEY_PRICE = new NamespacedKey(plugin, "tc_price");
        this.KEY_RES = new NamespacedKey(plugin, "tc_res");
    }

    public void open(Player viewer) {
        String title = colorize(root.getString("title", "&3Эпохи"));
        Inventory inv = Bukkit.createInventory(new AgesHolder(), 54, title);
        ConfigurationSection crystal = root.getConfigurationSection("crystal");
        int crystalSlot = crystal != null ? crystal.getInt("slot", 22) : 22;
        Material crystalMat = materialOf(crystal != null ? crystal.getString("material", "AMETHYST_SHARD") : "AMETHYST_SHARD");
        ItemStack crystalItem = new ItemStack(crystalMat);
        ItemMeta cm = crystalItem.getItemMeta();
        cm.setDisplayName(colorize(crystal != null ? crystal.getString("name", "&dКристалл Эпох") : "&dКристалл Эпох"));
        List<String> crystalLore = (crystal != null ? crystal.getStringList("lore") : Arrays.asList(
                "1",
                "1"
        )).stream().map(this::colorize).collect(Collectors.toList());
        cm.setLore(crystalLore);
        crystalItem.setItemMeta(cm);
        inv.setItem(crystalSlot, crystalItem);

        // Века по бокам
        ConfigurationSection agesSec = root.getConfigurationSection("ages");
        if (agesSec != null) {
            for (String key : agesSec.getKeys(false)) {
                ConfigurationSection a = agesSec.getConfigurationSection(key);
                if (a == null) continue;

                int slot = a.getInt("slot", -1);
                if (slot < 0 || slot >= inv.getSize()) continue;

                Material mat = materialOf(a.getString("item", "BOOK"));
                ItemStack stack = new ItemStack(mat);
                ItemMeta im = stack.getItemMeta();

                String displayName = colorize(a.getString("name", key));
                List<String> lore = new ArrayList<>();
                lore.addAll(a.getStringList("lore").stream().map(this::colorize).collect(Collectors.toList()));

                // Цена
                int money = a.getConfigurationSection("price") != null ? a.getConfigurationSection("price").getInt("money", 0) : 0;
                List<String> resourcesRaw = a.getConfigurationSection("price") != null
                        ? a.getConfigurationSection("price").getStringList("resources")
                        : new ArrayList<>();

                // Статус и подсказки
                String uiMoney = root.getString("ui.price_money", "&7Цена: &e%money%").replace("%money%", String.valueOf(money));
                String uiResHeader = root.getString("ui.price_resources_header", "&7Ресурсы:");
                String uiResLine = root.getString("ui.price_resource_line", "&8- &f%mat%&7: &e%amt%");
                String uiClick = root.getString("ui.click_to_upgrade", "&7Кликните для перехода");

                lore.add(colorize(uiMoney));
                if (!resourcesRaw.isEmpty()) {
                    lore.add(colorize(uiResHeader));
                    for (String line : resourcesRaw) {
                        String[] parts = line.split(":", 2);
                        if (parts.length != 2) continue;
                        String matName = parts[0].trim();
                        String amt = parts[1].trim();
                        lore.add(colorize(uiResLine.replace("%mat%", matName).replace("%amt%", amt)));
                    }
                }
                lore.add(colorize(uiClick));

                im.setDisplayName(displayName);
                im.setLore(lore);
                PersistentDataContainer pdc = im.getPersistentDataContainer();
                pdc.set(KEY_AGE, PersistentDataType.STRING, key);
                pdc.set(KEY_PRICE, PersistentDataType.INTEGER, money);
                pdc.set(KEY_RES, PersistentDataType.STRING, joinResources(resourcesRaw));

                stack.setItemMeta(im);
                inv.setItem(slot, stack);
            }
        }
        viewer.openInventory(inv);
    }
    public void handleClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof AgesHolder)) return;
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (!clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(KEY_AGE, PersistentDataType.STRING)) return;

        String targetAgeName = pdc.get(KEY_AGE, PersistentDataType.STRING);
        Integer priceMoney = pdc.get(KEY_PRICE, PersistentDataType.INTEGER);
        String resJoined = pdc.get(KEY_RES, PersistentDataType.STRING);

        Town town = towny.getTown(player);
        if (town == null) {
            player.sendMessage(messages.color("&cВы не состоите в городе."));
            return;
        }
        try {
            Age current = ageManager.getAge(town.getName());
            Age target = Age.valueOf(targetAgeName);
            if (current == target || current.ordinal() >= target.ordinal()) {
                player.sendMessage(messages.color("&eЭта эпоха уже открыта."));
                return;
            }
        } catch (Throwable ignored) {
        }

        int money = priceMoney != null ? priceMoney : 0;
        if (money > 0) {
            if (!towny.withdraw(town, money)) {
                player.sendMessage(messages.color("&cНедостаточно средств в казне."));
                return;
            }
        }
        Map<Material, Integer> need = parseResources(splitResources(resJoined));
        if (!need.isEmpty()) {
            if (!towny.getResources().hasAndTake(town, need)) {
                if (money > 0) towny.deposit(town, money);
                player.sendMessage(messages.color("&cНедостаточно ресурсов."));
                return;
            }
        }
        try {
            Age target = Age.valueOf(targetAgeName);
            ageManager.setAge(town.getName(), target);
            player.sendMessage(messages.color("&aЭпоха обновлена до " + target.name()));
        } catch (Throwable t) {
            if (money > 0) towny.deposit(town, money);
            player.sendMessage(messages.color("&cОшибка при обновлении эпохи."));
        }
    }
    private Map<Material, Integer> parseResources(List<String> raw) {
        Map<Material, Integer> out = new HashMap<>();
        if (raw == null) return out;
        for (String line : raw) {
            if (line == null || line.trim().isEmpty()) continue;
            String[] parts = line.split(":", 2);
            if (parts.length != 2) continue;
            Material m = Material.matchMaterial(parts[0].trim());
            try {
                int amount = Integer.parseInt(parts[1].trim());
                if (m != null && amount > 0) out.put(m, amount);
            } catch (NumberFormatException ignored) {}
        }
        return out;
    }
    private List<String> splitResources(String joined) {
        if (joined == null || joined.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(joined.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String joinResources(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return list.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(";"));
    }

    private Material materialOf(String name) {
        Material m = Material.matchMaterial(name == null ? "" : name.trim());
        return m != null ? m : Material.BOOK;
    }

    private String colorize(String s) {
        if (s == null) return "";
        String r = s
                .replace("<red>", ChatColor.RED.toString())
                .replace("<yellow>", ChatColor.YELLOW.toString())
                .replace("<green>", ChatColor.GREEN.toString())
                .replace("<light_purple>", ChatColor.LIGHT_PURPLE.toString())
                .replace("<dark_aqua>", ChatColor.DARK_AQUA.toString())
                .replace("<gray>", ChatColor.GRAY.toString())
                .replace("</>", ChatColor.RESET.toString());
        return ChatColor.translateAlternateColorCodes('&', r);
    }
    private static final class AgesHolder implements InventoryHolder {
        @Override public Inventory getInventory() { return null; }
    }
}
