//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

class MathFunctions
{
    public static int Cut(int a, int b) //Returns how many times a fits into b, +1 if there's remainder by division algorithm. Will return at least 1 fit.
    {
        int fits = 0;
        while( true )
        {
            if(b > a)
            {
                fits++;
                b -= a;
            }
            else if (b > 0)
            {
                fits++;
                break;
            }
            else return 1;
        }
        return fits;
    }
}

