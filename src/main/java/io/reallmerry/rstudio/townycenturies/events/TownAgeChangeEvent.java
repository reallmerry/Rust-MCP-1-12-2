package io.reallmerry.rstudio.townycenturies.events;

import io.reallmerry.rstudio.townycenturies.Age;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TownAgeChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String town;
    private final Age oldAge;
    private final Age newAge;

    public TownAgeChangeEvent(String town, Age oldAge, Age newAge) {
        this.town = town;
        this.oldAge = oldAge;
        this.newAge = newAge;
    }

    public String getTown() { return town; }
    public Age getOldAge() { return oldAge; }
    public Age getNewAge() { return newAge; }

    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}