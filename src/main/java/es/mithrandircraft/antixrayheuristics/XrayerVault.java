package es.mithrandircraft.antixrayheuristics;

import org.bukkit.entity.Player;

import java.util.List;

interface XrayerVault {
    String GetGUITitle();
    void PurgeAllXrayersAndRefreshVault();
    void XrayerDataRemover(String name, Boolean nameIsSolicitor);
    void SubstituteXrayerInfoLists(List<String> uuids, List<Integer> handledamounts, List<String> firsthandledtimes);
    void UpdateXrayerInfoLists(Player player, int page);
    void ClearXrayerInfoLists();

    void OpenVault(Player player, int page);
    void OpenXrayerConfiscatedInventory(Player player, int xrayerUUIDIndex);
    void TeleportToDetectionCoordinates(Player player, String xrayerUUID);
    int GetPage(String player);
    String GetInspectedXrayer(String player);
    void RemovePlayerAsViewer(String name);
    boolean CheckIfNoViewers();
}
