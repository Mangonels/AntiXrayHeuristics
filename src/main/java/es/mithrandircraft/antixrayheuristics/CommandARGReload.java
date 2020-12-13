//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGReload {
    public static void R(CommandSender sender, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Reload")) {
                //Do reload
                mainClass.reloadConfig(); //Reload main config
                LocaleManager.reload(); //Reload locale config
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("Reloaded")));
            } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else { //Is console
                //Do reload
                mainClass.reloadConfig();
                LocaleManager.reload();
                System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("Reloaded")));
        }
    }
}
