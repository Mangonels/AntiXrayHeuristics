//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import es.mithrandircraft.antixrayheuristics.math.IntVector3;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Arrays;

public class MiningSession { //Contains heuristics tracked per player

    //General distance/time algorithm variables:
    private float suspicionLevel = 0.0f; //Level of suspicion for the player

    Material lastMinedOre = null; //Last mined Material ore name

    Location lastMinedOreLocation = null; //Last mined Material ore location

    int minedNonOreBlocksStreak = 0; //Tracks how many non-ore blocks have been mined in streak.

    int foundAtZeroSuspicionStreak = 0; //Tracks how many times this mining session has been found at suspicion level 0 during Runnable tasks.

    //Mined blocks trail tracking algorithm variables:
    private IntVector3[] minedBlocksTrailCoords = new IntVector3[10];
    private int nextCoordsStorePos = 0; //Position where next mined block coordinates will be stored
    private int counterSinceLastBlockCoordsStore = 0; //Counts how many blocks we've mined since last mined block coordinates storing

    //General distance/time algorithm methods:
    public float GetSuspicionLevel() { return suspicionLevel; }
    void SetSuspicionLevel(float l)
    {
        suspicionLevel = l;
    }
    void AddSuspicionLevel(float i) { suspicionLevel += i; }

    //Mined blocks trail tracking algorithm methods:
    int GetLastBlockCoordsStoreCounter(){ return counterSinceLastBlockCoordsStore; }
    void CycleBlockCoordsStoreCounter(){ counterSinceLastBlockCoordsStore = (counterSinceLastBlockCoordsStore + 1) % 4; }
    void CycleNextCoordsStorePos(){ nextCoordsStorePos = (nextCoordsStorePos + 1) % 10; }
    int GetNextCoordsStorePos(){ return nextCoordsStorePos; }
    IntVector3 GetMinedBlocksTrailArrayPos(int pos) { return minedBlocksTrailCoords[pos]; }
    //void SetMinedBlocksTrailArrayPos(int pos, int x, int y, int z) { minedBlocksTrailCoords[pos] = new IntVector3(x, y, z); }
    void SetMinedBlocksTrailArrayPos(int pos, Location l) { minedBlocksTrailCoords[pos] = new IntVector3(l); }
    void ResetBlocksTrailArray() { Arrays.fill(minedBlocksTrailCoords, null); }
}
