//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.api;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import es.mithrandircraft.antixrayheuristics.callbacks.GetXrayerBelongingsCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AntiXrayHeuristicsAPIImpl implements AntiXrayHeuristicsAPI {

    private final AntiXrayHeuristics mainClassAccess;
    public AntiXrayHeuristicsAPIImpl(AntiXrayHeuristics mainClassRef)
    {
        mainClassAccess = mainClassRef;
    }

    //Declares specified player as an Xrayer and does configured handling
    public void Xrayer(String xrayername)
    {
        XrayerHandler.HandleXrayer(xrayername);
    }

    //Opens the vault for specified player
    public void Vault(Player player, int page)
    {
        mainClassAccess.vault.OpenVault(player, page);
    }

    //Purges the specified player from vault
    public void PurgePlayer(String player)
    {
        Player target = Bukkit.getServer().getPlayer(player);
        if (target != null) {
            mainClassAccess.vault.XrayerDataRemover(player, false);
        }
    }

    //Absolves a player with absolution handling and removes from vault
    public void AbsolvePlayer(String player)
    {
        Player target = Bukkit.getServer().getPlayer(player);
        if (target != null) { //Player online
            //return inventory to player
            final String targetUUID = target.getUniqueId().toString();
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(targetUUID, new GetXrayerBelongingsCallback()
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