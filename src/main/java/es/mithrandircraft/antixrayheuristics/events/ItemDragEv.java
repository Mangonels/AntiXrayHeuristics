package es.mithrandircraft.antixrayheuristics.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ItemDragEv implements Listener {
    @EventHandler
    public void dragEv(InventoryDragEvent e) //Stops items from being placed in top Xrayer Vault inventory by dragging them
    {
        if(e.getView().getTitle().equals("Xrayer Vault"))
        {
            e.setCancelled(true);
        }
    }
}
