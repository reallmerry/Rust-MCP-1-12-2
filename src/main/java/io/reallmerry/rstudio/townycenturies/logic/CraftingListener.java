package io.reallmerry.rstudio.townycenturies.logic;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.config.Messages;
import io.reallmerry.rstudio.townycenturies.hook.TownyHook;
import io.reallmerry.rstudio.townycenturies.logic.*;
import io.reallmerry.rstudio.townycenturies.data.TownAgeManager;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CraftingListener implements Listener {

    private final TownyCenturies plugin;
    private final TownAgeManager ages;
    private final AllowedRecipes allowed;
    private final TownyHook towny;
    private final Messages msg;
    private PolicyMode policy;

    public CraftingListener(TownyCenturies plugin,
                            TownAgeManager ages,
                            AllowedRecipes allowed,
                            TownyHook towny,
                            Messages msg,
                            PolicyMode policy) {
        this.plugin = plugin;
        this.ages = ages;
        this.allowed = allowed;
        this.towny = towny;
        this.msg = msg;
        this.policy = policy;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        CraftingInventory inv = e.getInventory();
        ItemStack result = inv.getResult();
        if (result == null || result.getType() == Material.AIR) return;
        Player p = firstPlayer(e.getViewers());
        if (p == null) return;

        Age current = currentAgeFor(p);
        Material mat = result.getType();

        if (!allowed.isAllowed(mat, current, policy)) {
            inv.setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        ItemStack result = e.getCurrentItem();
        if (result == null) return;

        Age current = currentAgeFor(p);
        Material mat = result.getType();
        if (!allowed.isAllowed(mat, current, policy)) {
            e.setCancelled(true);
            Age req = allowed.requiredAge(mat);
            msg.sendDenyCraft(p, mat.name(), req != null ? req : current);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent e) {
        ItemStack result = e.getResult();
        if (result == null || result.getType() == Material.AIR) return;
        HumanEntity he = e.getViewers().stream().filter(v -> v instanceof Player).findFirst().orElse(null);
        if (!(he instanceof Player)) return;
        Player p = (Player) he;

        Age current = currentAgeFor(p);
        Material mat = result.getType();
        if (!allowed.isAllowed(mat, current, policy)) {
            e.setResult(new ItemStack(Material.AIR));
        }
    }

    private Player firstPlayer(java.util.List<HumanEntity> viewers) {
        for (HumanEntity h : viewers) if (h instanceof Player) return (Player) h;
        return null;
    }

    private Age currentAgeFor(Player p) {
        String town = towny.getPlayerTownName(p);
        return ages.getAgeForTown(town, plugin.defaultAge());
    }
}