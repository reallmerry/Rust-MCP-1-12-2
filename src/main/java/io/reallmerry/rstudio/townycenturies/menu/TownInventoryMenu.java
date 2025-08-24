package io.reallmerry.rstudio.townycenturies.menu;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import io.reallmerry.rstudio.townycenturies.hook.TownyHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class TownInventoryMenu {
    private final TownyCenturies plugin;
    private final TownyHook towny;

    public TownInventoryMenu(TownyCenturies plugin, TownyHook towny) {
        this.plugin = plugin;
        this.towny = towny;
    }

    public void open(Player player, Town town, int page) {
        Inventory inv = Bukkit.createInventory(new TownInvHolder(town.getUUID(), page), 54,
                String.valueOf(plugin.miniMessage().deserialize(plugin.getConfig().getString("gui.town_inv.title", "<green>Инвентарь города")))
        );

        towny.getResources().fillPage(inv, town, page);
        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof TownInvHolder)) return;
        TownInvHolder holder = (TownInvHolder) e.getInventory().getHolder();
        if (e.getSlot() == plugin.getConfig().getInt("gui.town_inv.arrows.slot", 45)) {
            e.setCancelled(true);
            Town town = TownyAPI.getInstance().getTown(holder.townId);
            if (town == null) return;
            if (e.isLeftClick()) open((Player) e.getWhoClicked(), town, Math.max(0, holder.page - 1));
            else if (e.isRightClick()) open((Player) e.getWhoClicked(), town, holder.page + 1);
        }
    }

    private static final class TownInvHolder implements InventoryHolder {
        final UUID townId; final int page;
        TownInvHolder(UUID townId, int page) { this.townId = townId; this.page = page; }
        @Override public Inventory getInventory() { return null; }
    }
}

