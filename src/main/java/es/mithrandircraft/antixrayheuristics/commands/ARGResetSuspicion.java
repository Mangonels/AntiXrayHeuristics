//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.MiningSession;

import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGResetSuspicion {
    public static void RS(CommandSender sender) {//Non-parametrized
        if(sender instanceof Player) //Is player
        {
            Player player = (Player) sender;
            if(player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(player.getName());
                if (tempMS != null){
                    AntiXrayHeuristics.sessions.remove(player.getName());
                    player.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("OwnSuspicionNullified"));
                }
                else player.sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("NoOwnSuspicionReset"));
            }
            else player.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        } else //Is console
            System.out.println(LocaleManager.get().getString("PlayerOnlyCommand"));
    }

    public static void RS(CommandSender sender, String arg) //Parametrized
    {
        if (sender instanceof Player) { //Is player
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(arg);
                if (tempMS != null) {
                    AntiXrayHeuristics.sessions.remove(arg);
                    player.sendMessage(LocaleManager.get().getString("MessagesPrefix") + arg + LocaleManager.get().getString("PlayerSuspicionNullified"));
                } else
                    player.sendMessage(LocaleManager.get().getString("MessagesPrefix") + arg + LocaleManager.get().getString("NoPlayerSuspicionReset"));
            } else player.sendMessage(LocaleManager.get().getString("NoPermissionForCommand"));
        } else {
            MiningSession tempMS = AntiXrayHeuristics.sessions.get(arg);
            if (tempMS != null) {
                AntiXrayHeuristics.sessions.remove(arg);
                System.out.println(LocaleManager.get().getString("MessagesPrefix") + arg + LocaleManager.get().getString("PlayerSuspicionNullified"));
            } else //Is console
                System.out.println(LocaleManager.get().getString("MessagesPrefix") + arg + LocaleManager.get().getString("NoPlayerSuspicionReset"));
        }
    }
}
