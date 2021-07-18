//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

class LocaleManager {

    private static File localeFile;
    private static FileConfiguration localeConfiguration;

    private static void SetDefaultFileEntries() //Sets the default language entries in english
    {
        //Messages prefix:
        localeConfiguration.addDefault("MessagesPrefix","&5[&bAntiXrayHeuristics&5]");

        //Commands:
        localeConfiguration.addDefault("NoPermissionForCommand","&bYou do not have permission to execute this command.");
        localeConfiguration.addDefault("PlayerOnlyCommand","&bYou need to be a player in order to execute this command without arguments.");
        localeConfiguration.addDefault("InvalidCMDArg","&bInvalid command argument.");
        localeConfiguration.addDefault("PlayerNotOnlineOnHandle", "&bPlayer named &e{PlayerName} &bwas not found while attempting to handle as Xrayer. Player must be online.");
        localeConfiguration.addDefault("Reloaded", "&bAntiXrayHeuristics has reloaded.");
        localeConfiguration.addDefault("PlayerAbsolved", "&bAbsolving player, sending confiscated items back to owner and removing from vault.");
        localeConfiguration.addDefault("PlayerNotOnlineOnAbsolution", "&bPlayer wasn't online. You can only absolve online players in order to return their items.");
        localeConfiguration.addDefault("PlayerDataPurged", "&bThe Xrayer's data was purged from the xrayer vault.");
        localeConfiguration.addDefault("PurgeCommandLimit", "&bPurging players through command is currently impossible for players that are not currently connected to the server. You CAN however purge individual players that are offline through the ingame XrayerVault GUI: /axh v");
        localeConfiguration.addDefault("OwnSuspicionNullified", "&bYour suspicion has been nullified.");
        localeConfiguration.addDefault("PlayerSuspicionNullified", "&a{PlayerName} &b's suspicion has been nullified.");
        localeConfiguration.addDefault("NoOwnSuspicionReset", "&bYou are not suspicious of Xray usage. No suspicion reset applied.");
        localeConfiguration.addDefault("NoPlayerSuspicionReset", "&a{PlayerName} &bis not suspicious of Xray usage. No suspicion reset was applied.");
        localeConfiguration.addDefault("AbsolvedPlayer", "&a{PlayerName} &bhas been absolved from being considered an Xrayer, and his items have been returned.");

        //Plugin Actions:
        localeConfiguration.addDefault("AutoHandledPlayer", "&a{PlayerName} &bwas automatically registered and handled for xraying.");
        localeConfiguration.addDefault("PlayerMessageOnXray", "&bYou where punished for Xraying, uninstall Xrays immediately. Resume your gameplay only after you have uninstalled Xrays.");
        localeConfiguration.addDefault("ForcedPageZero", "&bYou where automatically sent to XrayerVault page 0 since one or various players where either purged or absolved.");
        localeConfiguration.addDefault("VaultRefreshed", "&bShowing latest xrayer information.");
        localeConfiguration.addDefault("TeleportToHandleLocation", "&bYou where teleported to the location where the player was handled for xrays.");

        //Persistent memory:
        localeConfiguration.addDefault("SQLDisconError", "&bThere was an error when attempting to disconnect from SQL database.");

        //GUI:
        localeConfiguration.addDefault("GUITitle", "&9Xrayer vault ");
        localeConfiguration.addDefault("BackButtonTitle", "&aBack");
        localeConfiguration.addDefault("GoBackButtonTitle", "&aGoBack");
        localeConfiguration.addDefault("NextButtonTitle", "&aNext");
        localeConfiguration.addDefault("PurgeButtonTitle", "&4Purge vault");
        localeConfiguration.addDefault("PurgeButtonDesc", Arrays.asList("&bThis will delete all xrayer", "&bentries within the vault.", "&bEntries are irrecoverable", "&bafter they have been purged."));
        localeConfiguration.addDefault("RefreshButtonTitle", "&9Refresh vault");
        localeConfiguration.addDefault("RefreshButtonDesc", Arrays.asList("&bRefreshes xrayer entries", "&bshowing new, modified, or", "&bremoved logged xrayers."));
        localeConfiguration.addDefault("PurgePlayerButtonTitle", "&4Purge player");
        localeConfiguration.addDefault("PurgePlayerButtonDesc", Arrays.asList("&bThis will delete this specific", "&bxrayer entry and inventory.", "&bEntries are irrecoverable", "&bafter they have been purged."));
        localeConfiguration.addDefault("AbsolvePlayerButtonTitle", "&eAbsolve player");
        localeConfiguration.addDefault("AbsolvePlayerButtonDesc", Arrays.asList("&bAbsolve this player,", "&breturning all confiscated", "&bitems. If they don't fit in", "&bplayer's inventory, they", "&bwill be dropped nearby.", "&bThis also purges the player's", "&bentry in this vault."));
        localeConfiguration.addDefault("HandledXrayerSlotName", "&aHandled xrayer slot #{Slot}");
        localeConfiguration.addDefault("EntryDesc", Arrays.asList("&aConsecutive times handled: &b{HandledTimesAmount}", "&aFirst time detected: &b{FirstTimeDetected}", "&aLast seen: &b{LastSeenTime}"));
        localeConfiguration.addDefault("EntryDescInspector", Arrays.asList("&aCLICK TO TELEPORT to detected location","&aConsecutive times handled: &b{HandledTimesAmount}", "&aFirst time detected: &b{FirstTimeDetected}", "&aLast seen: &b{LastSeenTime}"));
    }

    public static void setup(String pluginName) //Finds or generates custom config file
    {
        localeFile = new File(Bukkit.getServer().getPluginManager().getPlugin(pluginName).getDataFolder(), "locale.yml");

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






























