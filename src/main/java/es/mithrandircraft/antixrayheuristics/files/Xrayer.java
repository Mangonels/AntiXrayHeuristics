package es.mithrandircraft.antixrayheuristics.files;

import java.util.List;

public class Xrayer {

    public String UUID;
    public int Handled;
    public String FirstHandleTime;
    public String Belongings;

    public Xrayer(String uuid, int handled, String firsthandletime, String belongings) {
        UUID = uuid;
        Handled = handled;
        FirstHandleTime = firsthandletime;
        Belongings = belongings;
    }
}
