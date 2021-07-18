//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGXrayer {
    public static void X(CommandSender sender, AntiXrayHeuristics mainClass) { //Non-parametrized
        if (mainClass.getConfig().getBoolean("AddRandomDummyXrayerIfNoXrayerCommandParameters")){
            if (sender instanceof Player) //Is player
            {
                Player player = (Player) sender;
                if (player.hasPermission("AXH.Commands.Xrayer")) {
                    XrayerHandler.AddDummyXrayer();
                } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
            } else { //Is console
                XrayerHandler.AddDummyXrayer();
            }
        }
    }
    public static void X(CommandSender sender, String arg) { //Parametrized
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.Xrayer")) {
                XrayerHandler.HandleXrayer(arg);
            } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else { //Is console
            XrayerHandler.HandleXrayer(arg);
        }
    }
}