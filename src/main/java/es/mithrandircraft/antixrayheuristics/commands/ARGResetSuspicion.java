package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.MiningSession;

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
                    player.sendMessage("Your suspicion has been nullified.");
                }
                else player.sendMessage("You are not suspicious of Xray usage. No suspicion reset applied.");
            }
            else player.sendMessage("You do not have permission to execute this command.");
        }
        else System.out.println("You need to be a player in order to execute this command without arguments."); //Is console
    }

    public static void RS(CommandSender sender, String arg) //Parametrized
    {
        if (sender instanceof Player) { //Is player
            Player player = (Player) sender;
            if (player.hasPermission("AXH.Commands.ResetSuspicion")) {
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(arg);
                if (tempMS != null) {
                    AntiXrayHeuristics.sessions.remove(arg);
                    player.sendMessage(arg + "'s suspicion has been nullified.");
                } else
                    player.sendMessage(arg + " Is not suspicious of Xray usage. No suspicion reset was applied.");
            } else player.sendMessage("You do not have permission to execute this command.");
        } else {
            MiningSession tempMS = AntiXrayHeuristics.sessions.get(arg);
            if (tempMS != null) {
                AntiXrayHeuristics.sessions.remove(arg);
                System.out.println(arg + "'s suspicion has been nullified.");
            } else
                System.out.println(arg + " Is not suspicious of Xray usage. No suspicion reset was applied."); //Is console
        }
    }
}
