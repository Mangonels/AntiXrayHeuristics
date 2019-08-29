package es.mithrandircraft.antixrayheuristics;

public class PlaceholderManager {
    static String SubstitutePlaceholders(String toReplace, String player)
    {
        toReplace = toReplace.replaceAll("\\{PlayerName}", player);

        return toReplace;
    }
}