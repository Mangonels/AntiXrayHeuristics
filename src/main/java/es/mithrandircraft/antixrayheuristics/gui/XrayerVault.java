package es.mithrandircraft.antixrayheuristics.gui;

import es.mithrandircraft.antixrayheuristics.files.LocaleManager;
import es.mithrandircraft.antixrayheuristics.math.MathFunctions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class XrayerVault {

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    private ArrayList<String> UUIDs = new ArrayList<String>(); //These 3 list's values are parallel, and represent xrayer information.
    private ArrayList<Integer> handledAmmounts = new ArrayList<Integer>();
    private ArrayList<String> firstHandledTimes = new ArrayList<String>();

    private int pages; //How many pages the vault should have

    private HashMap<String, PlayerViewInfo> viewers = new HashMap<String, PlayerViewInfo>(); //Stores who's viewing the GUI and info on what's being looked at.

    private ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private ItemStack nextButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack prevButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack purgeButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private ItemStack refreshButton = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
    private ItemStack backButton = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
    private ItemStack purgePlayerButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private ItemStack absolvePlayerButton = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);

    public XrayerVault(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;

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
        purge_meta.setLore(Arrays.asList("This will delete all xrayer", "entries within the vault.", "Entries are irrecoverable", "after they have been purged."));
        purgeButton.setItemMeta(purge_meta);

        ItemMeta refresh_meta = refreshButton.getItemMeta();
        refresh_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("RefreshButtonTitle")));
        refresh_meta.setLore(LocaleManager.get().getStringList("RefreshButtonDesc"));
        refreshButton.setItemMeta(refresh_meta);

        ItemMeta back_meta = backButton.getItemMeta();
        back_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("GoBackButtonTitle")));
        backButton.setItemMeta(back_meta);

        ItemMeta purgeplayer_meta = purgePlayerButton.getItemMeta();
        purgeplayer_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("PurgePlayerButtonTitle")));
        purgeplayer_meta.setLore(Arrays.asList("This will delete this specific", " xrayer entry and inventory.", "Entries are irrecoverable", "after they have been purged."));
        purgePlayerButton.setItemMeta(purgeplayer_meta);

        ItemMeta absolveplayer_meta = absolvePlayerButton.getItemMeta();
        absolveplayer_meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',LocaleManager.get().getString("AbsolvePlayerButtonTitle")));
        absolveplayer_meta.setLore(Arrays.asList("Absolve this player,", "returning all confiscated", "items. If they don't fit in", "player's inventory, they", "will be dropped nearby.", "This also purges the player's", "entry in this vault."));
        absolvePlayerButton.setItemMeta(absolveplayer_meta);
    }

    public void PurgeAllXrayersAndRefreshVault() //Removes all xrayer data from both memory and xrayer vault, and refreshes vault after which sending everyone back to page 0 for safety
    {
        //Dump registered xrayers:
        mainClassAccess.mm.DeleteRegisteredXrayers();
        //refresh, just clear the vault lists since we did a global purge, and set pages to 1:
        ClearXrayerInfoLists();
        SetPages(1);
        //send viewers back to page 0:
        SendAllToPageZero();
    }

    public void ClearRegisteredXrayerAndRefreshVault(String name, Boolean nameIsSolicitor) //Clears the inspected xrayer that solicitorName is watching, and refreshes vault, sending everyone back to page 0 for safety
    {
        if(nameIsSolicitor) //Inputted name is the solicitor viewing xrayer through gui. We can get the xrayer's name since it's stored in this same vault
        {
            //purge player from database:
            mainClassAccess.mm.DeleteXrayer(GetInspectedXrayer(name));
            //remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(GetInspectedXrayer(name));
        }
        else //Should be the actual specific xrayer's name
        {
            //purge player from database:
            mainClassAccess.mm.DeleteXrayer(Bukkit.getServer().getPlayer(name).getUniqueId().toString());
            //remove absolved uuid from being listed in vault:
            RemoveXrayerDataByUUIDFromList(Bukkit.getServer().getPlayer(name).getUniqueId().toString());
        }
        //recalculate pages length:
        CalculatePages();
        //send viewers back to page 0:
        SendAllToPageZero();
    }

    public void UpdateXrayerInfoLists() //Updates Xrayer information arrays
    {
        ClearXrayerInfoLists(); //Clearing previous data

        mainClassAccess.mm.GetBaseXrayerData(UUIDs, handledAmmounts, firstHandledTimes); //This single function fills up the 3 lists with xrayer information.
    }

    public void ClearXrayerInfoLists() //clears all xrayer information arraylists
    {
        UUIDs.clear();
        handledAmmounts.clear();
        firstHandledTimes.clear();
    }

    public void CalculatePages() //Calculates pages considering the amount of registered xrayer uuid's, and that there can only be 27 results per page
    {
        pages = MathFunctions.Cut(45, UUIDs.size());
    }

    public void SendAllToPageZero() //Sends all viewers back to page 0:
    {
        for(Map.Entry<String, PlayerViewInfo> entry : viewers.entrySet())
        {
            OpenVault(Bukkit.getServer().getPlayer(entry.getKey()) ,0);
            Bukkit.getServer().getPlayer(entry.getKey()).sendMessage(LocaleManager.get().getString("MessagesPrefix") + " " + LocaleManager.get().getString("ForcedPageZero"));
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

    public void OpenVault(Player player, int page) //Opens xrayer vault in specified page (This shows player heads with info about the xrayer)
    {
        Inventory gui = Bukkit.createInventory(null, 54, "Xrayer Vault"); //Vault contents to display
        viewers.put(player.getName(), new PlayerViewInfo(page)); //Register player as gui viewer on a certain page (used as reference)

        //Fill up vault:
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        UUID currentUUID;
        int iteration = 0;
        ListIterator<String> iter = UUIDs.listIterator(page * 45);
        while (iter.hasNext() && !(iteration >= 45)) //Fills up the vault page with skulls containing xrayer data
        {
            currentUUID = UUID.fromString(iter.next());
            meta.setOwningPlayer(Bukkit.getServer().getOfflinePlayer(currentUUID)); //Assigns player to head owner
            meta.setDisplayName(Bukkit.getServer().getOfflinePlayer(currentUUID).getName()); //Head name editing
            meta.setLore(Arrays.asList("Times handled: " + handledAmmounts.get(iteration), "First detected: " + firstHandledTimes.get(iteration))); //Head lore editing
            skull.setItemMeta(meta);
            gui.setItem(iteration, skull);
            iteration++;
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

    public void OpenXrayerConfiscatedInventory(Player player, int xrayerUUIDIndex) //Opens an xrayer's confiscated inventory by xrayer name
    {
        viewers.get(player.getName()).xrayerInvUUID = UUIDs.get(xrayerUUIDIndex); //Update uuid of xrayer we're watching

        Inventory inv = Bukkit.createInventory(null, 54, "Xrayer Vault"); //Vault contents to display

        ItemStack[] confiscatedItems = mainClassAccess.mm.GetXrayerBelongings(UUIDs.get(xrayerUUIDIndex));

        //Fill up vault:
        for (int i = 0; i < 41; i++) //Fills up the vault page with confiscated xrayer's inventory
        {
            inv.setItem(i, confiscatedItems[i]);
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
        meta.setLore(Arrays.asList("Times handled: " + handledAmmounts.get(xrayerUUIDIndex), "First detected: " + firstHandledTimes.get(xrayerUUIDIndex))); //Head lore editing
        skull.setItemMeta(meta);
        inv.setItem(49, skull);

        player.openInventory(inv);
    }

    public int GetPage(String player) { return viewers.get(player).page; } //Returns page player is on
    public String GetInspectedXrayer(String player) { return viewers.get(player).xrayerInvUUID; } //Returns the uuid of the inventory player is inspecting (if any)

    public void RemoveXrayerDataByUUIDFromList(String uuid) //Removes all xrayer data in xrayer vault by uuid
    {
        int UUIDindex = UUIDs.indexOf(uuid); //Rest of xrayer data is in the same index, so we can use this index to delete all xrayer data in all parallel array lists
        UUIDs.remove(UUIDindex);
        handledAmmounts.remove(UUIDindex);
        firstHandledTimes.remove(UUIDindex);
    }
    public void RemovePlayerAsViewer(String name) { viewers.remove(name); } //Removes a player and it's data from the viewers hashmap

    public void SetPages(int many) { pages = many; } //Forcefully sets how many pages the vault currently has
}
