//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

class PlayerViewInfo {
    public int page;
    public String xrayerInvUUID;
    PlayerViewInfo(int pag)
    {
        page = pag; //Page Player is on
        xrayerInvUUID = null; //Current or last viewed inventory's xrayer uiid by Player
    }
}
