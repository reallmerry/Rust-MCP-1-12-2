package io.reallmerry.rstudio.townycenturies.events;

import io.reallmerry.rstudio.townycenturies.Age;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TownAgePreChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final String town;
    private final Age oldAge;
    private final Age newAge;
    private boolean cancelled;

    public TownAgePreChangeEvent(String town, Age oldAge, Age newAge) {
        this.town = town;
        this.oldAge = oldAge;
        this.newAge = newAge;
    }

    public String getTown() { return town; }
    public Age getOldAge() { return oldAge; }
    public Age getNewAge() { return newAge; }

    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancel) { this.cancelled = cancel; }

    @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}