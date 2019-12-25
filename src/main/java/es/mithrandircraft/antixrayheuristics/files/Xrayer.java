//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics.files;

public class Xrayer {

    public String UUID;
    public int Handled;
    public String FirstHandleTime;
    public String HandleLocation;
    public String Belongings;

    public Xrayer(String uuid, int handled, String firsthandletime, String handlelocation, String belongings) {
        UUID = uuid;
        Handled = handled;
        FirstHandleTime = firsthandletime;
        HandleLocation = handlelocation;
        Belongings = belongings;
    }
}
