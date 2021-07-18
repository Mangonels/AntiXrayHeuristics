//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

class EventBlockPlace implements Listener {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    public EventBlockPlace(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main) {
        this.mainClassAccess = main;
    }

    //Tells session for player name (if exists) that an explosive block was placed
    private void NotifySession(String playername)
    {
        MiningSession s = mainClassAccess.sessions.get(playername);
        if (s != null) s.IncreaseExplosivesPlaced();
    }

    @EventHandler
    public void BlockPlaceEvent(BlockPlaceEvent e) {
        //EXPLOSIVE ASSISTED MINING CHECK:
        //Check if the event occurred in one of the configured worlds:
        for (int i = 0; i < mainClassAccess.getConfig().getStringList("TrackWorlds").size(); i++) {
            if (mainClassAccess.getConfig().getStringList("TrackWorlds").get(i).equals(e.getBlock().getWorld().getName())) //It's one of the whitelisted "TrackWorlds"
            {
                //Is it overworld?:
                if (e.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL)
                {
                    //Relevant explosive materials check:
                    switch(e.getBlock().getType()) {
                        case TNT:
                        case END_CRYSTAL: {
                            NotifySession(e.getPlayer().getName());
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                    //Spigot for MC 1.16+
                    if(mainClassAccess.spigotVersion.version.GetValue() >= 116 && e.getBlock().getType() == Material.RESPAWN_ANCHOR){
                        NotifySession(e.getPlayer().getName());
                        break;
                    }
                }
                //Is it nether?:
                if (e.getBlock().getWorld().getEnvironment() == World.Environment.NETHER) {
                    //Relevant explosive materials check:
                    switch(e.getBlock().getType())
                    {
                        case TNT:
                        case END_CRYSTAL:
                        case WHITE_BED:
                        case BLACK_BED:
                        case GRAY_BED:
                        case LIGHT_GRAY_BED:
                        case BLUE_BED:
                        case CYAN_BED:
                        case LIGHT_BLUE_BED:
                        case YELLOW_BED:
                        case GREEN_BED:
                        case LIME_BED:
                        case ORANGE_BED:
                        case RED_BED:
                        case BROWN_BED:
                        case PURPLE_BED:
                        case PINK_BED:
                        case MAGENTA_BED: {
                            //Adapt suspicion for session
                            NotifySession(e.getPlayer().getName());
                            break;
                        }
                        default:
                        {
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}
