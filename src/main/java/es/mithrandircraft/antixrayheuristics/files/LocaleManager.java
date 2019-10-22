//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.files;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LocaleManager {
    private static File localeFile;
    private static FileConfiguration localeConfiguration;

    private static void SetDefaultFileEntries() //Sets the default language entries in english
    {
        //Messages prefix:
        localeConfiguration.addDefault("MessagesPrefix","[AntiXrayHeuristics]");

        //Commands:
        localeConfiguration.addDefault("NoPermissionForCommand","You do not have permission to execute this command.");
        localeConfiguration.addDefault("PlayerOnlyCommand","You need to be a player in order to execute this command without arguments.");
        localeConfiguration.addDefault("InvalidCMDArg","Invalid command argument.");

        //Actions:
        localeConfiguration.addDefault("Reloaded", "AntiXrayHeuristics has reloaded.");
        localeConfiguration.addDefault("PlayerAbsolved", "Absolving player, sent confiscated items back to player.");
        localeConfiguration.addDefault("PlayerNotOnlineOnAbsolution", "Player wasn't online. You can only absolve online players in order to return their items.");
        localeConfiguration.addDefault("PlayerDataPurged", "The Xrayer's data was purged from the xrayer vault.");
        localeConfiguration.addDefault("PurgeCommandLimit", "Purging players through command is currently impossible for players that are not currently connected to the server. You CAN however purge individual players that are offline through the ingame XrayerVault GUI: /axh v");
        localeConfiguration.addDefault("OwnSuspicionNullified", "Your suspicion has been nullified.");
        localeConfiguration.addDefault("PlayerSuspicionNullified", "'s suspicion has been nullified.");
        localeConfiguration.addDefault("NoOwnSuspicionReset", "You are not suspicious of Xray usage. No suspicion reset applied.");
        localeConfiguration.addDefault("NoPlayerSuspicionReset", " is not suspicious of Xray usage. No suspicion reset was applied.");
        localeConfiguration.addDefault("AutoHandledPlayer", "{PlayerName} was automatically registered and handled for xraying.");
        localeConfiguration.addDefault("NoPlayerSuspicionReset", " is not suspicious of Xray usage. No suspicion reset was applied.");
        localeConfiguration.addDefault("ForcedPageZero", "You where automatically sent and refreshed to XrayerVault page 0 since one or various players where either purged or absolved.");
        localeConfiguration.addDefault("SelfVaultViewRefresh", "You refreshed your vault view.");

        //SQL:
        localeConfiguration.addDefault("SQLDisconError", "There was an error when attempting to disconnect from SQL database.");
        localeConfiguration.addDefault("SQLTableFoundOrCreated", "SQL Xrayers table was either found or created from scratch. All seems to be in order.");

        //FileSystem:
        localeConfiguration.addDefault("JSONFileCreated", "JSON file created since none existed.");

        //GUI Button descriptions:
        localeConfiguration.addDefault("BackButtonTitle", "&aBack");
        localeConfiguration.addDefault("GoBackButtonTitle", "&aGoBack");
        localeConfiguration.addDefault("NextButtonTitle", "&aNext");
        localeConfiguration.addDefault("PurgeButtonTitle", "&4Purge vault");
        localeConfiguration.addDefault("PurgeButtonDesc", Arrays.asList("This will delete all xrayer", "entries within the vault.", "Entries are irrecoverable", "after they have been purged."));
        localeConfiguration.addDefault("RefreshButtonTitle", "&9Refresh vault");
        localeConfiguration.addDefault("RefreshButtonDesc", Arrays.asList("Refreshes xrayer entries", "showing new, modified, or", "removed logged xrayers."));
        localeConfiguration.addDefault("PurgePlayerButtonTitle", "&4Purge player");
        localeConfiguration.addDefault("PurgePlayerButtonDesc", Arrays.asList("This will delete this specific", "xrayer entry and inventory.", "Entries are irrecoverable", "after they have been purged."));
        localeConfiguration.addDefault("AbsolvePlayerButtonTitle", "&eAbsolve player");
        localeConfiguration.addDefault("AbsolvePlayerButtonDesc", Arrays.asList("Absolve this player,", "returning all confiscated", "items. If they don't fit in", "player's inventory, they", "will be dropped nearby.", "This also purges the player's", "entry in this vault."));
    }

    public static void setup() //Finds or generates custom config file
    {
        localeFile = new File(Bukkit.getServer().getPluginManager().getPlugin("AntiXrayHeuristics").getDataFolder(), "locale.yml");

        if(!localeFile.exists())
        {
            try {
                localeFile.createNewFile(); //Creates the file
            } catch(IOException e)
            {
                System.out.print("[AntiXrayHeuristics] Could not create locale file.");
            }
        }
        localeConfiguration = YamlConfiguration.loadConfiguration(localeFile);
        SetDefaultFileEntries(); //Sets default entries
    }

    public static FileConfiguration get()
    {
        return localeConfiguration;
    }

    public static void save()
    {
        try {
            localeConfiguration.save(localeFile);
        } catch(IOException e)
        {
            System.out.print("[AntiXrayHeuristics] Could not save locale file.");
        }
    }

    public static void reload() //Used from ARGReload AXH command argument in order to reload the language file
    {
        localeConfiguration = YamlConfiguration.loadConfiguration(localeFile);
    }
}






























