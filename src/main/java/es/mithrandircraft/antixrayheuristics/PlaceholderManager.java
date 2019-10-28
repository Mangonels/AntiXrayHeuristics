//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import java.util.List;

public class PlaceholderManager {
    public static String SubstitutePlayerNamePlaceholders(String toReplace, String player)
    {
        toReplace = toReplace.replaceAll("\\{PlayerName}", player);

        return toReplace;
    }

    public static List<String> SubstituteXrayerDataPlaceholders(List<String> toReplace, String handledTimesAmount, String firstHandleTime)
    {
        for(int i = 0; i < toReplace.size(); i++)
        {
            toReplace.set(i, toReplace.get(i).replaceAll("\\{HandledTimesAmount}", handledTimesAmount));
            toReplace.set(i, toReplace.get(i).replaceAll("\\{FirstTimeDetected}", firstHandleTime));
        }

        return toReplace;
    }
}