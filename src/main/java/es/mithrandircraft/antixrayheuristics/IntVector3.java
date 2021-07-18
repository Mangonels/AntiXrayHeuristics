//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Location;

class IntVector3 {
    private int X;
    private int Y;
    private int Z;

    public IntVector3(int x, int y, int z) //Initialize by each coordinate input
    {
        X = x;
        Y = y;
        Z = z;
    }
    public IntVector3(Location loc) //Initialize with location
    {
        X = loc.getBlockX();
        Y = loc.getBlockY();
        Z = loc.getBlockY();
    }

    //XYZ Getters
    public int GetX()
    {
        return X;
    }
    public int GetY()
    {
        return Y;
    }
    public int GetZ()
    {
        return Z;
    }

    //XYZ Setters
    public void SetX(int x) { X = x; }
    public void SetY(int y) { Y = y; }
    public void SetZ(int z) { Z = z; }
}
