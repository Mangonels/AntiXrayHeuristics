//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakEv implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public BlockBreakEv(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        //BLOCK MINING CHECK:
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < mainClassAccess.getConfig().getStringList("TrackWorlds").size(); i++) {
            if (mainClassAccess.getConfig().getStringList("TrackWorlds").get(i).equals(e.getBlock().getWorld().getName()) && e.getBlock().getLocation().getY() < mainClassAccess.getConfig().getInt("IgnoreHigherThanAltitude")) //It's one of the whitelisted "TrackWorlds", and altitude is lower than configured.
            {
                mainClassAccess.BBEventAnalyzer(e);
                break;
            }
        }
    }
}
