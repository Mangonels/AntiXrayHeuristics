//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandARGResetSuspicion {
    public static void RS(CommandSender sender, AntiXrayHeuristics mainClass) { //Non-parametrized
        if(sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if(player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = mainClass.sessions.get(player.getName());
                if (tempMS != null){
                    mainClass.sessions.remove(player.getName());
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("OwnSuspicionNullified")));
                }
                else player.sendMessage(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoOwnSuspicionReset")));
            }
            else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else //Is console
            System.out.println(LocaleManager.get().getString("PlayerOnlyCommand"));
    }

    public static void RS(CommandSender sender, String arg, AntiXrayHeuristics mainClass) //Parametrized
    {
        if (sender instanceof Player) { //Is player
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = mainClass.sessions.get(arg);
                if (tempMS != null) {
                    mainClass.sessions.remove(arg);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerSuspicionNullified"), arg));
                } else
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("NoPlayerSuspicionReset"), arg));
            } else player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("NoPermissionForCommand")));
        } else {
            MiningSession tempMS = mainClass.sessions.get(arg);
            if (tempMS != null) {
                mainClass.sessions.remove(arg);
                System.out.println(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("PlayerSuspicionNullified"), arg));
            } else //Is console
                System.out.println(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + PlaceholderManager.SubstitutePlayerNameAndColorCodePlaceholders(LocaleManager.get().getString("NoPlayerSuspicionReset"), arg));
        }
    }
}
