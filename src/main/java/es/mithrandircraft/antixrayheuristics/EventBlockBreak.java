//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

class EventBlockBreak implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public EventBlockBreak(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) { this.mainClassAccess = main; }

    @EventHandler
    public void blockBreakEvent(BlockBreakEvent e) {
        //BLOCK MINING CHECK:
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < mainClassAccess.getConfig().getStringList("TrackWorlds").size(); i++) {
            if (mainClassAccess.getConfig().getStringList("TrackWorlds").get(i).equals(e.getBlock().getWorld().getName())) //It's one of the whitelisted "TrackWorlds"
            {
                //Only consider if it's a normal overworld or nether environment:
                if(e.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL)
                {
                    if(e.getBlock().getLocation().getY() < mainClassAccess.getConfig().getInt("IgnoreHigherThanOverworldAltitude"))
                    {
                        mainClassAccess.BBEventAnalyzer(e);
                        break;
                    }
                }
                else if(e.getBlock().getWorld().getEnvironment() == World.Environment.NETHER)
                {
                    if(e.getBlock().getLocation().getY() < mainClassAccess.getConfig().getInt("IgnoreHigherThanNetherAltitude"))
                    {
                        mainClassAccess.BBEventAnalyzer(e);
                        break;
                    }
                }
                break;
            }
        }
    }
}