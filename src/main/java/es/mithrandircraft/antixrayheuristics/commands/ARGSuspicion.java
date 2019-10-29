//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.MiningSession;
import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ARGSuspicion {
    public static void S(CommandSender sender, AntiXrayHeuristics mainClass) {//Non-parametrized
        if (sender instanceof Player) { //Is player
            Player player = (Player) sender;
            MiningSession tempMS = mainClass.sessions.get(player.getName());
            if (tempMS != null) player.sendMessage("Your suspicion level: " + tempMS.GetSuspicionLevel());
            else player.sendMessage("You are not suspicious of Xray usage. No suspicion level available.");
        }
        else System.out.println(LocaleManager.get().getString("PlayerOnlyCommand")); //Is console
    }
    public static void S(CommandSender sender, String arg, AntiXrayHeuristics mainClass) //Parametrized
    {
        if (sender instanceof Player) { //Is player
            Player player = (Player) sender;
            MiningSession tempMS = mainClass.sessions.get(arg);
            if (tempMS != null)
                player.sendMessage(arg + "'s suspicion level: " + tempMS.GetSuspicionLevel());
            else
                player.sendMessage(arg + " Is not suspicious of Xray usage. No suspicion level available.");
        } else { //Is console
            MiningSession tempMS = mainClass.sessions.get(arg);
            if (tempMS != null)
                System.out.println(arg + "'s suspicion level: " + tempMS.GetSuspicionLevel());
            else
                System.out.println(arg + " Is not suspicious of Xray usage. No suspicion level available.");
        }
    }
}
