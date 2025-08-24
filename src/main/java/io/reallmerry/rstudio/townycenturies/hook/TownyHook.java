package io.reallmerry.rstudio.townycenturies.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.reallmerry.rstudio.townycenturies.TownyCenturies;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public class TownyHook {

    private final boolean present;

    public TownyHook(TownyCenturies plugin) {
        this.present = Bukkit.getPluginManager().getPlugin("Towny") != null;
        if (present) {
            plugin.getLogger().info("Towny detected. Town-based ages active.");
        } else {
            plugin.getLogger().warning("Towny not found. All players use default age.");
        }
    }

    public boolean isPresent() {
        return present;
    }

    public String getPlayerTownName(Player p) {
        if (!present) return null;
        Town town = TownyAPI.getInstance().getTown(p);
        return town != null ? town.getName() : null;
    }

    public boolean townInNation(String townName) {
        if (!present || townName == null) return false;
        Town town = TownyAPI.getInstance().getTown(townName);
        if (town == null) return false;
        Nation n = TownyAPI.getInstance().getTownNationOrNull(town);
        return n != null;
    }

    // ==== Новые методы для работы с AgesMenu ====

    /** Получить объект Town по игроку */
    public Town getTown(Player player) {
        if (!present) return null;
        return TownyAPI.getInstance().getTown(player);
    }

    /** Получить объект Town по UUID */
    public Town getTown(UUID uuid) {
        if (!present) return null;
        return TownyAPI.getInstance().getTown(uuid);
    }

    /** Снять деньги из банка города */
    public boolean withdraw(Town town, int amount) {
        if (!present || town == null) return false;
        try {
            town.getAccount().withdraw(amount, "Age upgrade");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Положить деньги в банк города */
    public void deposit(Town town, int amount) {
        if (!present || town == null) return;
        try {
            town.getAccount().deposit(amount, "Refund");
        } catch (Exception ignored) {}
    }

    /** Доступ к менеджеру ресурсов города */
    public TownResources getResources() {
        return new TownResources();
    }

    // ===== Примерный менеджер ресурсов — замени на свой =====
    public static class TownResources {
        /** Проверка и списание ресурсов */
        public boolean hasAndTake(Town town, Map<Material, Integer> need) {
            // TODO: реализуй под свой плагин/систему ресурсов
            return true;
        }

        /** Заполнение страницы ресурсов в инвентаре */
        public void fillPage(Inventory inv, Town town, int page) {
            // TODO: твоя реализация отображения ресурсов
        }
    }
}
