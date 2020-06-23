//--------------------------------------------------------------------
// Copyright © Dylan Calaf 2019 - AntiXrayHeuristics
//--------------------------------------------------------------------

/*
package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;


public class SpigotVersion {
    //Getting Versions enum from a representing integer:
    //Versions version = Versions.valueOf(112); // version = Versions.VERSION_1_12

    //Getting Versions integer ID from a representing enum:
    //Versions version = Versions.VERSION_1_12;
    //int versionInt = version.getValue(); // versionInt = 112

    public enum Versions { VERSION_UNKNOWN(0), VERSION_1_12(112), VERSION_1_13(113), VERSION_1_14(114), VERSION_1_15(115), VERSION_1_16(116);

        private int value;
        private static Map map = new HashMap<>();

        private Versions(int value) {
            this.value = value;
        }

        static {
            for (Versions version : Versions.values()) {
                map.put(version.value, version);
            }
        }

        public Versions ValueOf(int version) {
            return (Versions) map.get(version);
        }

        public int GetValue() {
            return value;
        }
    };


    public Versions version; //Captures Spigot's version on plugin start

    SpigotVersion()
    {
        version = GetBaseSpigotVersion();
    }

    public Versions GetBaseSpigotVersion() {
        String version = Bukkit.getVersion();
        if
        (
            version.endsWith("(MC: 1.12)") ||
            version.endsWith("(MC: 1.12.1)") ||
            version.endsWith("(MC: 1.12.2)")
        )
            return Versions.VERSION_1_12;
        else if
        (
            version.endsWith("(MC: 1.13)") ||
            version.endsWith("(MC: 1.13.1)") ||
            version.endsWith("(MC: 1.13.2)")
        )
            return Versions.VERSION_1_13;
        else if
        (
            version.endsWith("(MC: 1.14)") ||
            version.endsWith("(MC: 1.14.1)") ||
            version.endsWith("(MC: 1.14.2)") ||
            version.endsWith("(MC: 1.14.3)") ||
            version.endsWith("(MC: 1.14.4)")
        )
            return Versions.VERSION_1_14;
        else if
        (
            version.endsWith("(MC: 1.15)") ||
            version.endsWith("(MC: 1.15.1)") ||
            version.endsWith("(MC: 1.15.2)")
        )
            return Versions.VERSION_1_15;
        else if
        (
            version.endsWith("(MC: 1.16)")
        )
            return Versions.VERSION_1_16;

        else return Versions.VERSION_UNKNOWN;
    }
}
*/
