//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.entity.Player;

public interface APIAntiXrayHeuristics {

    //Declares specified player as an Xrayer and does configured handling
    void Xrayer(String xrayername);

    //Purges the specified player from vault
    void PurgePlayer(String playerName);

    //Absolves a player with absolution handling and removes from the player's vault registry
    void AbsolvePlayer(String playerName);
}