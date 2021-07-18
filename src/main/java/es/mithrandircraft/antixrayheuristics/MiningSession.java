//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Arrays;

class MiningSession { //Contains heuristics tracked per player

    private final es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics mainClassAccess;

    //General distance/time algorithm variables:
    private float suspicionLevel = 0.0f; //Level of suspicion for the player

    float suspicionDecreaseAmount = -4; //How much "suspicionLevel" to reduce for the this MiningSession every "mainRunnableFrequency" in AntiXrayHeuristics.java. This value results from a calculation based on speed.

    private Material lastMinedOre = null; //Last mined Material ore name

    private Location lastMinedOreLocation = null; //Last mined Material ore location

    private int shortestDeltaTimeThirtyBlocksMined = Integer.MAX_VALUE; //Shortest delta time from 0 to 30 blocks mined (this value represents the speed at which the mining session owner is removing blocks)

    private int lastThirtyBlocksTime; //Last time we reached 30 mined blocks

    private int thirtyBlockCounter = 0; //When this value reaches 30, "thirtyBlockTimer" is compared to "lowestTimeThirtyBlocksMined". If lower, "thirtyBlockTimer" replaces "lowestTimeThirtyBlocksMined"

    private int explosivesPlacedStreak = 0; //Tracks how many explosive blocks have been placed. Used for suspicion increase inmunity above certain threshold.

    public int minedNonOreBlocksStreak = 0; //Tracks how many non-ore blocks have been mined in streak.

    public int foundAtZeroSuspicionStreak = 0; //Tracks how many times this mining session has been found at suspicion level 0 during Runnable tasks.

    //Mined blocks trail tracking algorithm variables:
    private IntVector3[] minedBlocksTrailCoords = new IntVector3[10];
    private int nextCoordsStorePos = 0; //Position where next mined block coordinates will be stored
    private int counterSinceLastBlockCoordsStore = 0; //Counts how many blocks we've mined since last mined block coordinates storing

    MiningSession(es.mithrandircraft.antixrayheuristics.AntiXrayHeuristics main)
    {
        this.mainClassAccess = main;
        lastThirtyBlocksTime = (int)System.currentTimeMillis();
    }

    //General distance/time algorithm methods:
    public float GetSuspicionLevel() { return suspicionLevel; }
    public void SetSuspicionLevel(float l)
    {
        suspicionLevel = l;
    }
    public void AddSuspicionLevel(float l)
    {
        //Don't add suspicion if explosivesPlacedStreak above 4
        if(explosivesPlacedStreak <= 4) suspicionLevel += l;
    }
    public void SelfSuspicionReducer() { suspicionLevel += suspicionDecreaseAmount; }
    public void SetLastMinedOreData(Material m, Location l)
    {
        lastMinedOre = m;
        lastMinedOreLocation = l;
    }
    public Material GetLastMinedOre() { return lastMinedOre; }
    public Location GetLastMinedOreLocation() { return lastMinedOreLocation; }

    //Time property update methods:
    public void UpdateTimeAccountingProperties(Player p) //Updates properties based on time, and may also modify suspicion decrease amount based on them
    {
        thirtyBlockCounter++;
        if(thirtyBlockCounter >= 30)
        {
            int thirtyBlockDeltaTime = ((int)System.currentTimeMillis() - lastThirtyBlocksTime); //Thirty block delta time in milliseconds

            //Clamp 30 block delta time to max and min accountable millis:
            if(thirtyBlockDeltaTime > mainClassAccess.maxAccountableMillisecondDeltaForThirtyMinedBlocks)
                thirtyBlockDeltaTime = mainClassAccess.maxAccountableMillisecondDeltaForThirtyMinedBlocks;
            else if(thirtyBlockDeltaTime < mainClassAccess.minAccountableMillisecondDeltaForThirtyMinedBlocks)
                thirtyBlockDeltaTime = mainClassAccess.minAccountableMillisecondDeltaForThirtyMinedBlocks;

            //Is this new delta shorter (hence 30 blocks where mined faster at this point) than the current shortest registered delta time?
            if(thirtyBlockDeltaTime < shortestDeltaTimeThirtyBlocksMined)
            {
                shortestDeltaTimeThirtyBlocksMined = thirtyBlockDeltaTime; //New highest speed

                //Correlate decrease amount to current shortest delta...
                //Example formula for range conversion x in range [a,b] to y in range [c,d]: "y = (x - a) * ((d - c) / (b - a)) + c"
                suspicionDecreaseAmount = (shortestDeltaTimeThirtyBlocksMined - mainClassAccess.minAccountableMillisecondDeltaForThirtyMinedBlocks) *
                    ((mainClassAccess.minSuspicionDecreaseProportion - (mainClassAccess.maxSuspicionDecreaseProportion)) /
                    (mainClassAccess.maxAccountableMillisecondDeltaForThirtyMinedBlocks - mainClassAccess.minAccountableMillisecondDeltaForThirtyMinedBlocks)) +
                    (mainClassAccess.maxSuspicionDecreaseProportion);
            }
            thirtyBlockCounter = 0;
            lastThirtyBlocksTime = (int)System.currentTimeMillis();

            //Minimum suspicion decrease amount should be at (negative) least "absoluteMinimumSuspicionDecrease", else suspicion reduction is too slow. This prevents slow mining players from receiving fp's
            if(suspicionDecreaseAmount > mainClassAccess.absoluteMinimumSuspicionDecrease) suspicionDecreaseAmount = mainClassAccess.absoluteMinimumSuspicionDecrease;

            /*
            System.out.println("[AXH Debug]");
            System.out.println("Player: " + p.getName());
            System.out.println("Decrease amount: " + suspicionDecreaseAmount);
            System.out.println("Suspicion level: " + suspicionLevel);
            System.out.println("--");
            */
        }
    }

    //Mined blocks trail tracking algorithm methods:
    public void IncreaseExplosivesPlaced(){ explosivesPlacedStreak++; }
    public int GetLastBlockCoordsStoreCounter(){ return counterSinceLastBlockCoordsStore; }
    public void CycleBlockCoordsStoreCounter(){ counterSinceLastBlockCoordsStore = (counterSinceLastBlockCoordsStore + 1) % 4; }
    public void ResetBlockCoordsStoreCounter(){ counterSinceLastBlockCoordsStore = 0; }
    public void CycleNextCoordsStorePos(){ nextCoordsStorePos = (nextCoordsStorePos + 1) % 10; }
    public int GetNextCoordsStorePos(){ return nextCoordsStorePos; }
    public IntVector3 GetMinedBlocksTrailArrayPos(int pos) { return minedBlocksTrailCoords[pos]; }
    //void SetMinedBlocksTrailArrayPos(int pos, int x, int y, int z) { minedBlocksTrailCoords[pos] = new IntVector3(x, y, z); }
    public void SetMinedBlocksTrailArrayPos(int pos, Location l) { minedBlocksTrailCoords[pos] = new IntVector3(l); }
    public void ResetBlocksTrailArray() { Arrays.fill(minedBlocksTrailCoords, null); }
}
