//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InventoryCloseEv implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public InventoryCloseEv(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void closeEv(InventoryCloseEvent e) //Removes the player as an Xrayer Vault viewer with a delay after closing the Xrayer Vault inventory, only if player isn't still looking at inv.
    {
        if( e.getView().getTitle().equals("Xrayer Vault") )
        {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(); //Delayer scheduler
            DelayedInventoryCloseExecution delay = new DelayedInventoryCloseExecution(e.getPlayer(), mainClassAccess); //Class with the runnable run() method to be delayed
            executorService.schedule(delay, 500, TimeUnit.MILLISECONDS); //Set up the schedule to half a second delay
        }
    }
}
