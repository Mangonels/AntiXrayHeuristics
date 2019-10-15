//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Location;
import org.bukkit.Material;

public class MiningSession { //Contains heuristics tracked per player

    private float suspicionLevel = 0.0f; //Level of suspicion for the player

    public Material lastMinedOre = null; //Last mined Material ore name

    public Location lastMinedOreLocation = null; //Last mined Material ore location

    int minedNonOreBlocksStreak = 0; //Tracks how many non-ore blocks have been mined in streak.

    int foundAtZeroSuspicionStreak = 0; //Tracks how many times this mining session has been found at suspicion level 0 during Runnable tasks.

//    public int lastMinedOreTime; //Time since a last ore was mined
//
//    public int sameMinedOreVeinStreak = 0; //Ammount of times the same last mined ore has been mined
//
//    public int descentBlockMiningStreak = 0; //Streak ammount of blocks dug downwards, (?)resets if digging any other angle for a substantial period of time.
//
//    public int diagonalBlockMiningStreak = 0; //Streak ammount of blocks dug diagonally, (?)resets if digging any other angle, allthough harder to make reset due to the nature of an imperfect minecraft diagonal
//
//    public int straightBlockMiningStreak = 0; //Streak ammount of blocks dug horizontally, (?)resets if digging any other angle for a substantial period of time.

    public float GetSuspicionLevel() { return suspicionLevel; }
    public void SetSuspicionLevel(float l)
    {
        suspicionLevel = l;
    }
    public void AddSuspicionLevel(float i) { suspicionLevel += i; }
}
