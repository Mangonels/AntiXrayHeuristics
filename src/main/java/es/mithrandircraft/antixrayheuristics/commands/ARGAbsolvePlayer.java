//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.XrayerHandler;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGAbsolvePlayer {
    public static void A(CommandSender sender, String arg, AntiXrayHeuristics mainClass) {
        if (sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.AbsolvePlayer")) {
                Player target = Bukkit.getServer().getPlayer(arg);
                if (target != null) { //Player online
                    //return inventory to player
                    if (XrayerHandler.PlayerAbsolver(target.getUniqueId().toString(), mainClass.mm.GetXrayerBelongings(target.getUniqueId().toString()))) {
                        sender.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerAbsolved"));
                        //purge player from database:
                        mainClass.vault.ClearRegisteredXrayerAndRefreshVault(arg, false);
                    } else sender.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
                } else sender.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
            }
            else sender.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        }
        else { //Is console
            Player target = Bukkit.getServer().getPlayer(arg);
            if (target != null) { //Player online
                if (XrayerHandler.PlayerAbsolver(target.getUniqueId().toString(), mainClass.mm.GetXrayerBelongings(target.getUniqueId().toString()))) {
                    mainClass.vault.ClearRegisteredXrayerAndRefreshVault(arg, false);
                } else
                    System.out.print(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
            } else System.out.print(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("PlayerNotOnlineOnAbsolution"));
        }
    }
}
