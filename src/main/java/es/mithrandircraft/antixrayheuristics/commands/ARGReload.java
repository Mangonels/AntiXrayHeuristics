package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGReload {
    public static void R(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Reload")) {
                //Do reload
                mainClass.reloadConfig();
                player.sendMessage("AntiXrayHeuristics has reloaded.");
            } else player.sendMessage("You do not have permission to execute this command.");
        } else { //Is console
                //Do reload
                mainClass.reloadConfig();
                System.out.println("AntiXrayHeuristics has reloaded.");
        }
    }
}
