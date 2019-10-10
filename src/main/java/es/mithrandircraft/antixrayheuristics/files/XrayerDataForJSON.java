package es.mithrandircraft.antixrayheuristics.files;

public class XrayerDataForJSON {
    private String UUID;
    private String Handled;
    private String FirstHandleTime;
    private String Belongings;

    public String getUUID(){
        return UUID;
    }
    public void setUUID(String uuid){
        this.UUID = uuid;
    }

    public String getHandled(){
        return Handled;
    }
    public void setHandled(String handled){
        this.Handled = handled;
    }

    public String getFirstHandleTime(){
        return FirstHandleTime;
    }
    public void setFirstHandleTime(String firsthandletime){
        this.FirstHandleTime = firsthandletime;
    }

    public String getBelongings(){
        return Belongings;
    }
    public void setBelongings(String belongings){
        this.Belongings = belongings;
    }
}
