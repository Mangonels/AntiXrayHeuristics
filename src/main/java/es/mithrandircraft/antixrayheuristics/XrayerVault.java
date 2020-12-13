//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

class XrayerVault {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    //These 3 list's values are parallel, and represent xrayer information.
    private ArrayList<String> UUIDs = new ArrayList<String>();
    private ArrayList<Integer> handledAmounts = new ArrayList<Integer>();
    private ArrayList<String> firstHandledTimes = new ArrayList<String>();

    private int pages; //How many pages the vault should have

    private String GUITitle;

    private HashMap<String, PlayerViewInfo> viewers = new HashMap<String, PlayerViewInfo>(); //Stores who's viewing the GUI and info on what's being looked at.

    private ItemStack separator;
    private ItemStack nextButton;
    private ItemStack prevButton;
    private ItemStack purgeButton;
    private ItemStack refreshButton;
    private ItemStack backButton;
    private ItemStack purgePlayerButton;
    private ItemStack absolvePlayerButton;

    public String GetGUITitle()
    {
        return GUITitle;
    }

    public XrayerVault(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;

        GUITitle = PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getString("GUITitle"));

        separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        nextButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        prevButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        purgeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        refreshButton = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        backButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        purgePlayerButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        absolvePlayerButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);

        ItemMeta separator_meta = separator.getItemMeta();
        separator_meta.setDisplayName(" ");
        separator.setItemMeta(separator_meta);

        ItemMeta next_meta = nextButton.getItemMeta();
        next_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("NextButtonTitle")));
        nextButton.setItemMeta(next_meta);

        ItemMeta prev_meta = prevButton.getItemMeta();
        prev_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("BackButtonTitle")));
        prevButton.setItemMeta(prev_meta);

        ItemMeta purge_meta = purgeButton.getItemMeta();
        purge_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("PurgeButtonTitle")));
        purge_meta.setLore(PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getStringList("PurgeButtonDesc")));
        purgeButton.setItemMeta(purge_meta);

        ItemMeta refresh_meta = refreshButton.getItemMeta();
        refresh_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("RefreshButtonTitle")));
        refresh_meta.setLore(PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getStringList("RefreshButtonDesc")));
        refreshButton.setItemMeta(refresh_meta);

        ItemMeta back_meta = backButton.getItemMeta();
        back_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("GoBackButtonTitle")));
        backButton.setItemMeta(back_meta);

        ItemMeta purgeplayer_meta = purgePlayerButton.getItemMeta();
        purgeplayer_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("PurgePlayerButtonTitle")));
        purgeplayer_meta.setLore(PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getStringList("PurgePlayerButtonDesc")));
        purgePlayerButton.setItemMeta(purgeplayer_meta);

        ItemMeta absolveplayer_meta = absolvePlayerButton.getItemMeta();
        absolveplayer_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("AbsolvePlayerButtonTitle")));
        absolveplayer_meta.setLore(PlaceholderManager.SubstituteColorCodePlaceholders(LocaleManager.get().getStringList("AbsolvePlayerButtonDesc")));
        absolvePlayerButton.setItemMeta(absolveplayer_meta);
    }

    //Removes all xrayer data from both memory and xrayer vault, and refreshes vault after which sending everyone back to page 0 for safety
    public void PurgeAllXrayersAndRefreshVault()
    {
        //Dump registered xrayers:
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.DeleteRegisteredXrayers());
        //Refresh, just clear the vault lists since we did a global purge, and set pages to 1:
        ClearXrayerInfoLists();
        SetPages(1);
        //Send viewers back to page 0:
        SendAllToPageZero();
    }

    //Can clear the inspected xrayer that solicitor name is watching (or explicitly defined if nameIsSolicitor = false) from both persistent data storage and data loaded in RAM for vault, and refresh vault, sending everyone back to page 0 for safety
    public void XrayerDataRemover(String name, Boolean nameIsSolicitor)
    {
        final String xrayerUUID;
        if(nameIsSolicitor) //Inputted name is the solicitor viewing xrayer through gui. We can get the xrayer's name since it's stored in this same vault
        {
            //Purge player from memory:
            xrayerUUID = GetInspectedXrayer(name);
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.DeleteXrayer(xrayerUUID));
            //Remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(xrayerUUID);
        }
        else //Should be the actual specific xrayer's name
        {
            //Purge player from memory:
            xrayerUUID = Bukkit.getServer().getPlayer(name).getUniqueId().toString();
            Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.DeleteXrayer(xrayerUUID));
            //Remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(xrayerUUID);
        }
        //Recalculate pages length:
        CalculatePages();
        //Send viewers back to page 0:
        SendAllToPageZero();
    }

    //Clears data arrays and fills with input data
    public void SubstituteXrayerInfoLists(List<String> uuids, List<Integer> handledamounts, List<String> firsthandledtimes)
    {
        //Clear previous data:
        ClearXrayerInfoLists();

        //Update with new data:
        UUIDs.addAll(uuids);
        handledAmounts.addAll(handledamounts);
        firstHandledTimes.addAll(firsthandledtimes);
    }

    //Updates Xrayer information arrays through "SubstituteXayerInfoLists" and also forces page open for player if not null
    public void UpdateXrayerInfoLists(Player player, int page)
    {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetAllBaseXrayerData(new CallbackGetAllBaseXrayerData() {
            @Override
            public void onQueryDone() {
                OpenVault(player, page);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("VaultRefreshed")));
            }
        })); //This single async function fills up the 3 lists with xrayer information.
    }

    //Clears all xrayer information arraylists
    public void ClearXrayerInfoLists()
    {
        UUIDs.clear();
        handledAmounts.clear();
        firstHandledTimes.clear();
    }

    //Calculates pages considering the amount of registered xrayer uuid's, and that there can only be 27 results per page
    private void CalculatePages()
    {
        pages = MathFunctions.Cut(45, UUIDs.size());
    }

    //Sends all vault viewers back to page 0
    private void SendAllToPageZero()
    {
        for(Map.Entry<String, PlayerViewInfo> entry : viewers.entrySet())
        {
            OpenVault(Bukkit.getServer().getPlayer(entry.getKey()) ,0);
            Bukkit.getServer().getPlayer(entry.getKey()).sendMessage(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("ForcedPageZero")));
        }
    }

    //Button format getter methods:
    public ItemStack GetNextButtonFormat(){ return nextButton; }
    public ItemStack GetPrevButtonFormat(){ return prevButton; }
    public ItemStack GetPurgeButtonFormat(){ return purgeButton; }
    public ItemStack GetRefreshButtonFormat(){ return refreshButton; }

    public ItemStack GetBackButtonFormat(){ return backButton; }
    public ItemStack GetPurgePlayerButtonFormat(){ return purgePlayerButton; }
    public ItemStack GetAbsolvePlayerButtonFormat(){ return absolvePlayerButton; }

    //Opens xrayer vault for player in specified page (The vault shows player heads with info about the xrayer)
    public void OpenVault(Player player, int page)
    {
        //Recalculate pages length:
        CalculatePages();

        Inventory gui = Bukkit.createInventory(null, 54, GUITitle + (page+1) + "/" + pages);
        viewers.put(player.getName(), new PlayerViewInfo(page)); //Register player as gui viewer on a certain page (used as player-page reference)

        //Fill up vault:
        ItemStack entry = new ItemStack(Material.LIME_CONCRETE);
        int iterations;
        if((page+1) == pages)
        {
            iterations = UUIDs.size() % 45;
            if(iterations == 0) iterations = 45;
        }
        else iterations = 45;
        for(int i = 0; i < iterations; i++) //Fills up the vault page with entry buttons
        {
            ItemMeta entryMeta = entry.getItemMeta();
            entryMeta.setDisplayName(PlaceholderManager.SubstituteXrayerSlotAndColorCodePlaceholders(LocaleManager.get().getString("HandledXrayerSlotName"), i + 45 * page));
            entry.setItemMeta(entryMeta);
            gui.setItem(i, entry);
        }

        //Lower section separators:
        gui.setItem(46, separator);
        gui.setItem(47, separator);
        gui.setItem(49, separator);
        gui.setItem(51, separator);
        gui.setItem(52, separator);

        //Lower section vault/gui stuff:
        if(page + 1 < pages) gui.setItem(53, nextButton); else gui.setItem(53, separator);
        if(page - 1 > -1) gui.setItem(45, prevButton); else gui.setItem(45, separator);
        gui.setItem(48, purgeButton);
        gui.setItem(50, refreshButton);

        player.openInventory(gui);
    }

    //Opens an xrayer's confiscated inventory by xrayer name
    public void OpenXrayerConfiscatedInventory(Player player, int xrayerUUIDIndex)
    {
        viewers.get(player.getName()).xrayerInvUUID = UUIDs.get(xrayerUUIDIndex); //Update uuid of xrayer we're watching

        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerBelongings(UUIDs.get(xrayerUUIDIndex), new CallbackGetXrayerBelongings()
        {
            @Override
            public void onQueryDone(ItemStack[] belongings)
            {
                Inventory inv = Bukkit.createInventory(null, 54, GUITitle); //Vault contents to display

                if(belongings != null) //Belongings could be null when extracted from database, since StoreCopy option exists and can be false
                {
                    //Fill up vault:
                    for (int i = 0; i < 41; i++) //Fills up the vault page with confiscated xrayer's inventory
                    {
                        inv.setItem(i, belongings[i]);
                    }
                }

                //Separation bar for mere decoration:
                for(int i = 45; i < 54; i++) { inv.setItem(i, separator); }
                inv.setItem(46, separator);
                inv.setItem(47, separator);
                inv.setItem(48, separator);
                inv.setItem(50, separator);
                inv.setItem(52, separator);

                //Lower section vault/gui stuff:
                inv.setItem(45, backButton);
                inv.setItem(51, purgePlayerButton);
                inv.setItem(53, absolvePlayerButton);

                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex)))); //Assigns player to head owner
                meta.setDisplayName(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getName()); //Head name editing
                Date lastSeenDate = new Date(Bukkit.getServer().getOfflinePlayer(UUID.fromString(UUIDs.get(xrayerUUIDIndex))).getLastPlayed()); //Getting the last played date as Date object, and then formatting it...
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                meta.setLore(PlaceholderManager.SubstituteXrayerDataAndColorCodePlaceholders(LocaleManager.get().getStringList("PlayerHeadDescInspector"), String.valueOf(handledAmounts.get(xrayerUUIDIndex)), firstHandledTimes.get(xrayerUUIDIndex), df.format(lastSeenDate))); //Head lore editing
                skull.setItemMeta(meta);
                inv.setItem(49, skull);

                player.openInventory(inv);
            }
        }));
    }

    //Teleports the player to the coordinates where an xrayer was detected for xraying
    public void TeleportToDetectionCoordinates(Player player, String xrayerUUID)
    {
        Bukkit.getScheduler().runTaskAsynchronously(mainClassAccess, () -> mainClassAccess.mm.GetXrayerHandleLocation(xrayerUUID, new CallbackGetXrayerHandleLocation()
        {
            @Override
            public void onQueryDone(Location handlelocation)
            {
                player.teleport(handlelocation);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("MessagesPrefix")) + " " + ChatColor.translateAlternateColorCodes('&', LocaleManager.get().getString("TeleportToHandleLocation")));
            }
        }));
    }

    //Returns page player is on
    public int GetPage(String player) { return viewers.get(player).page; }

    //Returns the uuid of the original owner of the inventory player is inspecting (if any)
    public String GetInspectedXrayer(String player) { return viewers.get(player).xrayerInvUUID; }

    //Removes all xrayer data in xrayer vault by uuid
    private void RemoveXrayerDataByUUIDFromList(String uuid)
    {
        //Check if uuid exists:
        for (int i = 0; i < UUIDs.size(); i++) if(UUIDs.get(i).equals(uuid)) {
            //All xrayer data is in the same index, so we can use this index to delete all xrayer data in all parallel array lists
            UUIDs.remove(i);
            handledAmounts.remove(i);
            firstHandledTimes.remove(i);
            break;
        }
    }

    //Removes a player and it's data from the viewers hashmap
    public void RemovePlayerAsViewer(String name) { viewers.remove(name); }

    //Returns true if the viewers HashMap is empty, else false
    public boolean CheckIfNoViewers(){ return viewers.isEmpty(); }

    //Forcefully sets howMany pages the vault currently has
    private void SetPages(int howMany) { pages = howMany; }
}