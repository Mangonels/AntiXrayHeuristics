//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2020 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

class PlayerViewInfo {
    public int page; //Page Player is on
    public String xrayerInvUUID; //Current or last viewed inventory's xrayer uiid by Player
    PlayerViewInfo(int pag)
    {
        page = pag;
        xrayerInvUUID = null;
    }
}
