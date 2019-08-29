package es.mithrandircraft.antixrayheuristics.commands;

import es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics;
import es.mithrandircraft.antixrayheuristics.MiningSession;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XraySuspicion implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) //Non parametrized command:
        {
            if (sender instanceof Player) { //Is player
                Player player = (Player) sender;
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(player.getName());
                if (tempMS != null) player.sendMessage("Your suspicion level: " + tempMS.GetSuspicionLevel());
                else player.sendMessage("You are not suspicious of Xray usage. No suspicion level available.");
            }
            else System.out.println("You need to be a player in order to execute this command without arguments."); //Is console
        }
        else //Parametrized command:
        {
            if (sender instanceof Player) { //Is player
                Player player = (Player) sender;
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(args[0]);
                if (tempMS != null)
                    player.sendMessage(args[0] + "'s suspicion level: " + tempMS.GetSuspicionLevel());
                else
                    player.sendMessage(args[0] + " Is not suspicious of Xray usage. No suspicion level available.");
            } else { //Is console
                MiningSession tempMS = AntiXrayHeuristics.sessions.get(args[0]);
                if (tempMS != null)
                    System.out.println(args[0] + "'s suspicion level: " + tempMS.GetSuspicionLevel());
                else
                    System.out.println(args[0] + " Is not suspicious of Xray usage. No suspicion level available.");
            }
        }
   return false;
   }
}
