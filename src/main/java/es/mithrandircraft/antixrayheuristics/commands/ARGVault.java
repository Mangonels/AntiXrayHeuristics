//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ARGVault {
    static void V(CommandSender sender, AntiXrayHeuristics mainClass)
    {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Vault")) {
                mainClass.vault.UpdateXrayerInfoLists(player, 0); //Update all of the xrayer's uuid's in vault's arraylist (Arguments imply also opening vault for player on page)

            } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else { //Is console
            System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerOnlyCommand")));
        }
    }
}
