//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.entity.HumanEntity;

class DelayedInventoryCloseExecution implements Runnable {
    private HumanEntity p;
    private AntiXrayHeuristics mainClassAccess;

    DelayedInventoryCloseExecution(HumanEntity player, AntiXrayHeuristics mca)
    {
        p = player;
        mainClassAccess = mca;
    }
    public void run() //Xrayer vault cleanup, and additional cleanup if no one is inspecting the vault:
    {
        if(!(p.getOpenInventory().getTitle().equals("Xrayer Vault"))) {
            mainClassAccess.vault.RemovePlayerAsViewer(p.getName()); //Remove the player as viewer inconditionally
            //Clear loaded xrayer information in vault from RAM if no one is still viewing the GUI:
            if(mainClassAccess.vault.CheckIfNoViewers())
            {
                mainClassAccess.vault.ClearXrayerInfoLists(false);
            }
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
