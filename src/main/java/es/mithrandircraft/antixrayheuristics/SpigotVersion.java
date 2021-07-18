//--------------------------------------------------------------------
// Copyright © Dylan Calaf Latham 2019-2021 AntiXrayHeuristics
//--------------------------------------------------------------------

package es.mithrandircraft.antixrayheuristics;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;


class SpigotVersion {
    //Getting Versions enum from a representing integer:
    //Versions version = Versions.valueOf(112); // version = Versions.VERSION_1_12

    //Getting Versions integer ID from a representing enum:
    //Versions version = Versions.VERSION_1_12;
    //int versionInt = version.getValue(); // versionInt = 112

    public enum Versions { VERSION_UNKNOWN(0), VERSION_1_8(108), VERSION_1_9(109), VERSION_1_10(110), VERSION_1_11(111),
        VERSION_1_12(112), VERSION_1_13(113), VERSION_1_14(114), VERSION_1_15(115), VERSION_1_16(116), VERSION_1_17(117),
        VERSION_1_18(118), VERSION_1_19(119), VERSION_1_20(120), VERSION_1_21(121), VERSION_1_22(122), VERSION_1_23(123),
        VERSION_1_24(124), VERSION_1_25(125), VERSION_1_26(126), VERSION_1_27(127), VERSION_1_28(128), VERSION_1_29(129),
        VERSION_1_30(130), VERSION_1_31(131), VERSION_1_32(132), VERSION_1_33(133), VERSION_1_34(134), VERSION_1_35(135),
        VERSION_1_36(136), VERSION_1_37(137), VERSION_1_38(138), VERSION_1_39(139), VERSION_1_40(140), VERSION_1_41(141),
        VERSION_1_42(142);

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
                version.contains("MC: 1.8")
        )
            return Versions.VERSION_1_8;
        else if
        (
                version.contains("MC: 1.9")
        )
            return Versions.VERSION_1_9;
        else if
        (
                version.contains("MC: 1.10")
        )
            return Versions.VERSION_1_10;
        else if
        (
                version.contains("MC: 1.11")
        )
            return Versions.VERSION_1_11;
        else if
        (
                version.contains("MC: 1.12")
        )
            return Versions.VERSION_1_12;
        else if
        (
                version.contains("MC: 1.13")
        )
            return Versions.VERSION_1_13;
        else if
        (
                version.contains("MC: 1.14")
        )
            return Versions.VERSION_1_14;
        else if
        (
                version.contains("MC: 1.15")
        )
            return Versions.VERSION_1_15;
        else if
        (
                version.contains("MC: 1.16")
        )
            return Versions.VERSION_1_16;
        else if
        (
                version.contains("MC: 1.17")
        )
            return Versions.VERSION_1_17;
        else if
        (
                version.contains("MC: 1.18")
        )
            return Versions.VERSION_1_18;
        else if
        (
                version.contains("MC: 1.19")
        )
            return Versions.VERSION_1_19;
        else if
        (
                version.contains("MC: 1.20")
        )
            return Versions.VERSION_1_20;
        else if
        (
                version.contains("MC: 1.21")
        )
            return Versions.VERSION_1_21;
        else if
        (
                version.contains("MC: 1.22")
        )
            return Versions.VERSION_1_22;
        else if
        (
                version.contains("MC: 1.23")
        )
            return Versions.VERSION_1_23;
        else if
        (
                version.contains("MC: 1.24")
        )
            return Versions.VERSION_1_24;
        else if
        (
                version.contains("MC: 1.25")
        )
            return Versions.VERSION_1_25;
        else if
        (
                version.contains("MC: 1.26")
        )
            return Versions.VERSION_1_26;
        else if
        (
                version.contains("MC: 1.27")
        )
            return Versions.VERSION_1_27;
        else if
        (
                version.contains("MC: 1.28")
        )
            return Versions.VERSION_1_28;
        else if
        (
                version.contains("MC: 1.29")
        )
            return Versions.VERSION_1_29;
        else if
        (
                version.contains("MC: 1.30")
        )
            return Versions.VERSION_1_30;
        else if
        (
                version.contains("MC: 1.31")
        )
            return Versions.VERSION_1_31;
        else if
        (
                version.contains("MC: 1.32")
        )
            return Versions.VERSION_1_32;
        else if
        (
                version.contains("MC: 1.33")
        )
            return Versions.VERSION_1_33;
        else if
        (
                version.contains("MC: 1.34")
        )
            return Versions.VERSION_1_34;
        else if
        (
                version.contains("MC: 1.35")
        )
            return Versions.VERSION_1_35;
        else if
        (
                version.contains("MC: 1.36")
        )
            return Versions.VERSION_1_36;
        else if
        (
                version.contains("MC: 1.37")
        )
            return Versions.VERSION_1_37;
        else if
        (
                version.contains("MC: 1.38")
        )
            return Versions.VERSION_1_38;
        else if
        (
                version.contains("MC: 1.39")
        )
            return Versions.VERSION_1_39;
        else if
        (
                version.contains("MC: 1.40")
        )
            return Versions.VERSION_1_40;
        else if
        (
                version.contains("MC: 1.41")
        )
            return Versions.VERSION_1_41;
        else if
        (
                version.contains("MC: 1.42")
        )
            return Versions.VERSION_1_42;

        else return Versions.VERSION_UNKNOWN;
    }
}