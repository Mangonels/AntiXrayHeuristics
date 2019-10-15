//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.events;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import org.bukkit.entity.HumanEntity;

public class DelayedInventoryCloseExecution implements Runnable {
    private HumanEntity p;
    private AntiXrayHeuristics mainClassAccess;

    DelayedInventoryCloseExecution(HumanEntity player, AntiXrayHeuristics mca)
    {
        p = player;
        mainClassAccess = mca;
    }
    public void run() //Removes the player as an Xrayer Vault viewer, only if player isn't still looking at Xrayer Vault inv:
    {
        if(!(p.getOpenInventory().getTitle().equals("Xrayer Vault"))) {
            mainClassAccess.vault.RemovePlayerAsViewer(p.getName());
            if(mainClassAccess.getConfig().getString("StorageMethod").equals("JSON"))
            {
                //Flush stored xrayer data from MemoryManager in RAM if no one is still viewing the GUI:
                if(mainClassAccess.vault.CheckIfNoViewers())
                {
                    mainClassAccess.mm.JSONFlushLoadedXrayerData();
                }
            }
        }
    }
}
