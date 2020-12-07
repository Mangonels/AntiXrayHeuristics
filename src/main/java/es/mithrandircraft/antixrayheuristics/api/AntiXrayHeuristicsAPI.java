//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.api;

import org.bukkit.entity.Player;

public interface AntiXrayHeuristicsAPI {

    //Declares specified player as an Xrayer and does configured handling
    public void Xrayer(String xrayername);

    //Opens the vault for specified player
    public void Vault(Player player, int page);

    //Purges the specified player from vault
    public void PurgePlayer(String player);

    //Absolves a player with absolution handling and removes from vault
    public void AbsolvePlayer(String player);
}
