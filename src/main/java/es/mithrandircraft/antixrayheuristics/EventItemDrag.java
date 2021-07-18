//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

class EventItemDrag implements Listener {
    @EventHandler
    public void dragEv(InventoryDragEvent e) //Stops items from being placed in top Xrayer Vault inventory by dragging them
    {
        if(e.getView().getTitle().equals("Xrayer Vault"))
        {
            e.setCancelled(true);
        }
    }
}
