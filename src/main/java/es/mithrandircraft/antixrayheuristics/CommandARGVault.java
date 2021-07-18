//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGVault {
    static void V(CommandSender sender, AntiXrayHeuristics mainClass)
    {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Vault")) {
                mainClass.vault.UpdateXrayerInfoLists(player, 0); //Update all of the xrayer's uuid's in vault's arraylist (arguments imply also opening vault for player on page)

            } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else { //Is console
            System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("PlayerOnlyCommand")));
        }
    }
}
