//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.ChatColor;

import java.util.List;

public class PlaceholderManager {
    public static String SubstitutePlayerNameAndColorCodePlaceholders(String toReplace, String player)
    {
        toReplace = toReplace.replaceAll("\\{PlayerName}", player);
        toReplace = ChatColor.translateAlternateColorCodes('&', toReplace);

        return toReplace;
    }

    public static String SubstitutePlayerNameAndHandleTimesPlaceholders(String toReplace, String player, String handleTimes)
    {
        toReplace = toReplace.replaceAll("\\{PlayerName}", player);
        toReplace = toReplace.replaceAll("\\{TimesDetected}", handleTimes);

        return toReplace;
    }

    public static List<String> SubstituteColorCodePlaceholders(List<String> toReplace)
    {
        for(int i = 0; i < toReplace.size(); i++)
        {
            toReplace.set(i, ChatColor.translateAlternateColorCodes('&', toReplace.get(i)));
        }

        return toReplace;
    }

    public static List<String> SubstituteXrayerDataAndColorCodePlaceholders(List<String> toReplace, String handledTimesAmount, String firstHandleTime, String lastSeenTime)
    {
        for(int i = 0; i < toReplace.size(); i++)
        {
            toReplace.set(i, toReplace.get(i).replaceAll("\\{HandledTimesAmount}", handledTimesAmount));
            toReplace.set(i, toReplace.get(i).replaceAll("\\{FirstTimeDetected}", firstHandleTime));
            toReplace.set(i, toReplace.get(i).replaceAll("\\{LastSeenTime}", lastSeenTime));
            toReplace.set(i, ChatColor.translateAlternateColorCodes('&', toReplace.get(i)));
        }

        return toReplace;
    }
}