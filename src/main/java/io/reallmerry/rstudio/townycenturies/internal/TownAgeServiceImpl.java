package io.reallmerry.rstudio.townycenturies.internal;

import io.reallmerry.rstudio.townycenturies.Age;
import io.reallmerry.rstudio.townycenturies.api.TownAgeService;
import io.reallmerry.rstudio.townycenturies.data.TownAgeManager;
import io.reallmerry.rstudio.townycenturies.events.TownAgeChangeEvent;
import io.reallmerry.rstudio.townycenturies.events.TownAgePreChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginManager;

public class TownAgeServiceImpl implements TownAgeService {

    private final TownAgeManager ages;
    private final PluginManager pm = Bukkit.getPluginManager();

    public TownAgeServiceImpl(TownAgeManager ages) {
        this.ages = ages;
    }

    @Override
    public Age getTownAge(String townName) {
        return ages.getAgeForTown(townName, Age.STONE);
    }

    @Override
    public void setTownAge(String townName, Age age) {
        Age old = getTownAge(townName);
        ages.setAgeForTown(townName, age);
        pm.callEvent(new TownAgeChangeEvent(townName, old, age));
    }

    @Override
    public boolean setTownAgeValidated(String townName, Age newAge) {
        Age old = getTownAge(townName);
        TownAgePreChangeEvent pre = new TownAgePreChangeEvent(townName, old, newAge);
        pm.callEvent(pre);
        if (pre.isCancelled()) {
            return false;
        }
        ages.setAgeForTown(townName, newAge);
        pm.callEvent(new TownAgeChangeEvent(townName, old, newAge));
        return true;
    }

    @Override
    public Age getTownAgeForPlayer(OfflinePlayer player) {
        // Здесь можно интегрировать Towny API, чтобы находить город игрока
        return Age.STONE;
    }
}