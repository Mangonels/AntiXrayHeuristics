package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ARGVault {
    static void V(CommandSender sender, AntiXrayHeuristics mainClass)
    {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Vault")) {
                mainClass.vault.UpdateUUIDList(); //Update all of the xrayer's uuid's in vault's arraylist
                mainClass.vault.OpenVault(player, 0);

            } else player.sendMessage("You do not have permission to execute this command.");
        } else { //Is console
            System.out.println("You need to be a player in order to execute this command.");
        }
    }
}
