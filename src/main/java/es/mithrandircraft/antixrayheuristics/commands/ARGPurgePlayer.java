//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGPurgePlayer {
    public static void P(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.PurgePlayer")) {
                //purge xrayer from database:
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    mainClass.vault.ClearRegisteredXrayerAndRefreshVault(arg, false);
                    sender.sendMessage(LocaleManager.get().getString("PlayerDataPurged"));
                }
                else player.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PurgeCommandLimit"));
            } else player.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        } else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) {
                mainClass.vault.ClearRegisteredXrayerAndRefreshVault(arg, false);
            }
            else System.out.print(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PurgeCommandLimit"));
        }
    }
}