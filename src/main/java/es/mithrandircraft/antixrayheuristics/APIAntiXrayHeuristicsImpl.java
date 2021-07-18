//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class APIAntiXrayHeuristicsImpl implements APIAntiXrayHeuristics {

    private final AntiXrayHeuristics mainClassAccess;
    public APIAntiXrayHeuristicsImpl(AntiXrayHeuristics mainClassRef)
    {
        mainClassAccess = mainClassRef;
    }

    //Declares specified player as an Xrayer and does configured handling
    public void Xrayer(String xrayerName)
    {
        XrayerHandler.HandleXrayer(xrayerName);
    }

    //Purges the specified player from vault
    public void PurgePlayer(String playerName)
    {
        Player target = Bukkit.getServer().getPlayer(playerName);
        if (target != null) {
            mainClassAccess.vault.XrayerDataRemover(playerName, false);
        }
    }

    //Absolves a player with absolution handling and removes from the player's vault registry
    public void AbsolvePlayer(String player)
    {
        Player target = Bukkit.getServer().getPlayer(player);
        if (target != null) { //Player online
            //Return inventory to player
            final String targetUUID = target.getUniqueId().toString();
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(targetUUID, new CallbackGetXrayerBelongings()
            {
                @Override
                public void onQueryDone(ItemStack[] belongings)
                {
                    if (XrayerHandler.PlayerAbsolver(targetUUID, belongings, mainClassAccess)) {
                        mainClassAccess.vault.XrayerDataRemover(player, false);
                    }
                }
            }));
        }
    }
}