package io.reallmerry.rstudio.townycenturies.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public final class MenuListener implements Listener {

    private final AgesMenu agesMenu;
    private final TownInventoryMenu townInvMenu;

    public MenuListener(AgesMenu agesMenu, TownInventoryMenu townInvMenu) {
        this.agesMenu = agesMenu;
        this.townInvMenu = townInvMenu;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // Передаём событие в оба меню — внутри они сами проверят, их это или нет
        try {
            agesMenu.handleClick(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            townInvMenu.handleClick(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        // Если в будущем понадобится что‑то делать при закрытии конкретных меню —
        // сюда можно добавить вызовы наподобие agesMenu.handleClose(e)
    }
}
