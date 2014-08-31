/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import static net.hontvari.bukkitplugin.UbiCraft.varosWorldName;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 *
 * @author attila
 */
public class Warps {

    @Warp(desc = "A jövőbeli UbiCraft szerver spawnja")
    public static final Location spawn = pos(varosWorldName, -29.5, 50, 20, -180);

    @Warp(desc = "Kedved szerint vághatsz akáciát, nyírfát, tölgyfát, dzsungelfát. ")
    public static final Location fatelep = pos(varosWorldName, 8022.5, 50, 8027, 180);

    @Warp(desc = "Cukornád, búza, krumpli, répa egy helyen. ")
    public static final Location mezőgazdaság = pos(varosWorldName, -28.5, 50, -27, -270);
    @Warp(desc = "Ló, bárány, tehén, malac, csirke. ")
    public static final Location állatfarm = pos(varosWorldName, 5.5, 50, -68, -90);

    @Warp(desc = "Ugrálj szigeteken!")
    public static final Location skypvp = pos("SkyPvP", 1.5, 65, 9.5, 0);

    @Warp(desc = "Jönnek a zombik rátok, az nyer, aki legtovább bent marad. ")
    public static final Location zombierun = pos(varosWorldName, -10.5, 50, -18, -90);

    @Warp(desc = "Player Vs Player", before = "  ")
    public static final Location pvp = pos(varosWorldName, -24.5, 50, -41, -180);

    @Warp(desc = "Normális, alap Minecraft világ. ")
    public static final Location vadon = pos("Vadon", 51.5, 73, 44.5, -270);

    @Warp(desc = "Állatkiállítás. ")
    public static final Location szobrok = pos(varosWorldName, 34.5, 52, -39.5, -90);

    @Warp(desc = "Mine20 és zsohajdu1 jumpjai. ")
    public static final Location jumpok = pos(varosWorldName, -50.5, 46, -4, 0);

//    @Warp(desc = "Mobokkal harcolsz, ennyi. ")
//    public static final Location mobarena = pos("world", 20201, 62, 19986, 90);
    @Warp(desc = "A Telkek világ warpja. ", after = ", ")
    public static final Location telkek = pos("plotworld", 16, 53, 13, 180);

    @Warp(desc = "Ásd ki a másik alól a havat, hogy az leessen!", after = "")
    public static final Location spleef = pos(varosWorldName, 9.5, 50, -30, 270);

    private static Location pos(String world, double i, double i0, double i1, int f) {
        return new Location(Bukkit.getWorld(world), i, i0, i1, f, 0);
    }

    public static final Location fatelepPortal = pos(varosWorldName, -4.5, 50, -42, -270);
}
