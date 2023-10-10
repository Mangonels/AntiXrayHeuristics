package es.mithrandircraft.antixrayheuristics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

class CommandAXHAutoCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if(args.length == 1)
        {
            List<String> firstArguments = new ArrayList<>();

            firstArguments.add("vault");
            firstArguments.add("reload");
            firstArguments.add("resetsuspicion");
            firstArguments.add("xrayer");
            firstArguments.add("absolve");
            firstArguments.add("purge");
            firstArguments.add("suspicion");

            return firstArguments;
        }

        return null;
    }
}
