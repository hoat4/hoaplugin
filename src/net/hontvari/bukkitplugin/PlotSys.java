/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.block.Sign;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.i18n;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
/**
 *
 * @author attila
 */
public class PlotSys {

    private final Properties hoadata;

    public PlotSys(Properties hoadata) {
        this.hoadata = hoadata;
    }

    public World load() {
        World plotworld = new WorldCreator("plotworld").generateStructures(false).generator(new PlotWorldGenerator()).createWorld();
        Bukkit.getWorlds().add(plotworld);
        return plotworld;
    }

    public void claim(Player p) {
        int x = p.getLocation().getBlockX() / 32;
        int z = p.getLocation().getBlockZ() / 32;
        System.out.println(x + ", " + z);
        String coords = x + "|" + z;
        if (!p.isOp() && hoadata.containsKey("plotsys.by.plotcoords." + coords)) {
            chat(p, "§4"+String.format(i18n("plotsys.alreadyBought"), "§c" + hoadata.getProperty("plotsys.by.plotcoords." + coords)));
            return;
        }
        Plot plot = new Plot(x, z, p);
        plot.placeSign();
        hoadata.setProperty("plotsys.mainplot." + p.getName(), coords);
        hoadata.setProperty("plotsys.by.plotcoords." + coords, p.getDisplayName());
        chat(p, "§2" + i18n("plotsys.bought"));
    }

    public void goHome(Player p) {
        String[] xz = hoadata.getProperty("plotsys.mainplot." + p.getName()).split(Pattern.quote("|"));
        int x = Integer.decode(xz[0]);
        int z = Integer.decode(xz[1]);

        p.teleport(new Location(Bukkit.getWorld("plotworld"), x * 32 + 29.5, 50, z * 32 + 33, 180, 0));
    }

    public void aboutPlotSys(Player p) {
        chat(p, "plotsys.about.1");
        chat(p, "plotsys.about.2");
    }

    private static class PlotWorldGenerator extends ChunkGenerator {

        private int coordsToInt(int x, int y, int z) {
            return (x * 16 + z) * 128 + y;
        }

        @Override
        public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
            byte[] blocks = new byte[32768];
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 50; y++)
                    for (int z = 0; z < 16; z++)
                        /*if (chunkX % 2 == 0 && chunkZ % 2 == 0)
                         blocks[coordsToInt(x, y, z)] = 95;
                         else if (chunkX % 2 == 1 && chunkZ % 2 == 1)
                         blocks[coordsToInt(x, y, z)] = 45;
                         else*/
                        blocks[coordsToInt(x, y, z)] = 2;
            if (chunkX < 0 || chunkZ < 0) {
                chunkX = Math.abs(chunkX);
                chunkZ = Math.abs(chunkZ);
            }

            if (chunkX % 2 == 0 && chunkZ % 2 == 0) {
                left(blocks);
                up(blocks);
                blocks[coordsToInt(1, 50, 0)] = 0;
                blocks[coordsToInt(1, 50, 15)] = 44;
            } else if (chunkX % 2 == 0 && chunkZ % 2 == 1) {
                left(blocks);
                down(blocks);
                blocks[coordsToInt(1, 50, 0)] = 44;
                blocks[coordsToInt(15, 50, 0)] = 0;
                blocks[coordsToInt(1, 50, 15)] = 0;
            } else if (chunkX % 2 == 1 && chunkZ % 2 == 0) {
                right(blocks);
                up(blocks);
                blocks[coordsToInt(0, 50, 1)] = 44;
                blocks[coordsToInt(15, 50, 1)] = 0;
            } else if (chunkX % 2 == 1 && chunkZ % 2 == 1) {
                right(blocks);
                down(blocks);
                blocks[coordsToInt(0, 50, 14)] = 44;
                blocks[coordsToInt(15, 50, 14)] = 0;
                blocks[coordsToInt(14, 50, 15)] = 0;
                blocks[coordsToInt(14, 50, 0)] = 44;
                blocks[coordsToInt(14, 50, 15)] = 68;
            }
            return blocks;
        }

        private void left(byte[] blocks) {
            // bal út
            blocks[coordsToInt(0, 49, 0)] = (byte) 155;
            blocks[coordsToInt(0, 49, 1)] = (byte) 155;
            blocks[coordsToInt(0, 49, 2)] = (byte) 155;
            blocks[coordsToInt(0, 49, 3)] = (byte) 155;
            blocks[coordsToInt(0, 49, 4)] = (byte) 155;
            blocks[coordsToInt(0, 49, 5)] = (byte) 155;
            blocks[coordsToInt(0, 49, 6)] = (byte) 155;
            blocks[coordsToInt(0, 49, 7)] = (byte) 155;
            blocks[coordsToInt(0, 49, 8)] = (byte) 155;
            blocks[coordsToInt(0, 49, 9)] = (byte) 155;
            blocks[coordsToInt(0, 49, 10)] = (byte) 155;
            blocks[coordsToInt(0, 49, 11)] = (byte) 155;
            blocks[coordsToInt(0, 49, 12)] = (byte) 155;
            blocks[coordsToInt(0, 49, 13)] = (byte) 155;
            blocks[coordsToInt(0, 49, 14)] = (byte) 155;
            blocks[coordsToInt(0, 49, 15)] = (byte) 155;

            // bal kerítés
            blocks[coordsToInt(1, 50, 1)] = (byte) 44;
            blocks[coordsToInt(1, 50, 2)] = (byte) 44;
            blocks[coordsToInt(1, 50, 3)] = (byte) 44;
            blocks[coordsToInt(1, 50, 4)] = (byte) 44;
            blocks[coordsToInt(1, 50, 5)] = (byte) 44;
            blocks[coordsToInt(1, 50, 6)] = (byte) 44;
            blocks[coordsToInt(1, 50, 7)] = (byte) 44;
            blocks[coordsToInt(1, 50, 8)] = (byte) 44;
            blocks[coordsToInt(1, 50, 9)] = (byte) 44;
            blocks[coordsToInt(1, 50, 10)] = (byte) 44;
            blocks[coordsToInt(1, 50, 11)] = (byte) 44;
            blocks[coordsToInt(1, 50, 12)] = (byte) 44;
            blocks[coordsToInt(1, 50, 13)] = (byte) 44;
            blocks[coordsToInt(1, 50, 14)] = (byte) 44;
            blocks[coordsToInt(1, 50, 15)] = (byte) 44;
        }

        private void right(byte[] blocks) {
            // bal út
            blocks[coordsToInt(15, 49, 0)] = (byte) 155;
            blocks[coordsToInt(15, 49, 1)] = (byte) 155;
            blocks[coordsToInt(15, 49, 2)] = (byte) 155;
            blocks[coordsToInt(15, 49, 3)] = (byte) 155;
            blocks[coordsToInt(15, 49, 4)] = (byte) 155;
            blocks[coordsToInt(15, 49, 5)] = (byte) 155;
            blocks[coordsToInt(15, 49, 6)] = (byte) 155;
            blocks[coordsToInt(15, 49, 7)] = (byte) 155;
            blocks[coordsToInt(15, 49, 8)] = (byte) 155;
            blocks[coordsToInt(15, 49, 9)] = (byte) 155;
            blocks[coordsToInt(15, 49, 10)] = (byte) 155;
            blocks[coordsToInt(15, 49, 11)] = (byte) 155;
            blocks[coordsToInt(15, 49, 12)] = (byte) 155;
            blocks[coordsToInt(15, 49, 13)] = (byte) 155;
            blocks[coordsToInt(15, 49, 14)] = (byte) 155;
            blocks[coordsToInt(15, 49, 15)] = (byte) 155;

            // bal kerítés
            blocks[coordsToInt(14, 50, 1)] = (byte) 44;
            blocks[coordsToInt(14, 50, 2)] = (byte) 44;
            blocks[coordsToInt(14, 50, 3)] = (byte) 44;
            blocks[coordsToInt(14, 50, 4)] = (byte) 44;
            blocks[coordsToInt(14, 50, 5)] = (byte) 44;
            blocks[coordsToInt(14, 50, 6)] = (byte) 44;
            blocks[coordsToInt(14, 50, 7)] = (byte) 44;
            blocks[coordsToInt(14, 50, 8)] = (byte) 44;
            blocks[coordsToInt(14, 50, 9)] = (byte) 44;
            blocks[coordsToInt(14, 50, 10)] = (byte) 44;
            blocks[coordsToInt(14, 50, 11)] = (byte) 44;
            blocks[coordsToInt(14, 50, 12)] = (byte) 44;
            blocks[coordsToInt(14, 50, 13)] = (byte) 44;
            blocks[coordsToInt(14, 50, 14)] = (byte) 44;
            blocks[coordsToInt(14, 50, 15)] = (byte) 44;
        }

        private void up(byte[] blocks) {
            // felső út
            blocks[coordsToInt(0, 49, 0)] = (byte) 155;
            blocks[coordsToInt(1, 49, 0)] = (byte) 155;
            blocks[coordsToInt(2, 49, 0)] = (byte) 155;
            blocks[coordsToInt(3, 49, 0)] = (byte) 155;
            blocks[coordsToInt(4, 49, 0)] = (byte) 155;
            blocks[coordsToInt(5, 49, 0)] = (byte) 155;
            blocks[coordsToInt(6, 49, 0)] = (byte) 155;
            blocks[coordsToInt(7, 49, 0)] = (byte) 155;
            blocks[coordsToInt(8, 49, 0)] = (byte) 155;
            blocks[coordsToInt(9, 49, 0)] = (byte) 155;
            blocks[coordsToInt(10, 49, 0)] = (byte) 155;
            blocks[coordsToInt(11, 49, 0)] = (byte) 155;
            blocks[coordsToInt(12, 49, 0)] = (byte) 155;
            blocks[coordsToInt(13, 49, 0)] = (byte) 155;
            blocks[coordsToInt(14, 49, 0)] = (byte) 155;
            blocks[coordsToInt(15, 49, 0)] = (byte) 155;

            // felső kerítés
            blocks[coordsToInt(1, 50, 1)] = (byte) 44;
            blocks[coordsToInt(2, 50, 1)] = (byte) 44;
            blocks[coordsToInt(3, 50, 1)] = (byte) 44;
            blocks[coordsToInt(4, 50, 1)] = (byte) 44;
            blocks[coordsToInt(5, 50, 1)] = (byte) 44;
            blocks[coordsToInt(6, 50, 1)] = (byte) 44;
            blocks[coordsToInt(7, 50, 1)] = (byte) 44;
            blocks[coordsToInt(8, 50, 1)] = (byte) 44;
            blocks[coordsToInt(9, 50, 1)] = (byte) 44;
            blocks[coordsToInt(10, 50, 1)] = (byte) 44;
            blocks[coordsToInt(11, 50, 1)] = (byte) 44;
            blocks[coordsToInt(12, 50, 1)] = (byte) 44;
            blocks[coordsToInt(13, 50, 1)] = (byte) 44;
            blocks[coordsToInt(14, 50, 1)] = (byte) 44;
            blocks[coordsToInt(15, 50, 1)] = (byte) 44;
        }

        private void down(byte[] blocks) {
            // alsó út
            blocks[coordsToInt(0, 49, 15)] = (byte) 155;
            blocks[coordsToInt(1, 49, 15)] = (byte) 155;
            blocks[coordsToInt(2, 49, 15)] = (byte) 155;
            blocks[coordsToInt(3, 49, 15)] = (byte) 155;
            blocks[coordsToInt(4, 49, 15)] = (byte) 155;
            blocks[coordsToInt(5, 49, 15)] = (byte) 155;
            blocks[coordsToInt(6, 49, 15)] = (byte) 155;
            blocks[coordsToInt(7, 49, 15)] = (byte) 155;
            blocks[coordsToInt(8, 49, 15)] = (byte) 155;
            blocks[coordsToInt(9, 49, 15)] = (byte) 155;
            blocks[coordsToInt(10, 49, 15)] = (byte) 155;
            blocks[coordsToInt(11, 49, 15)] = (byte) 155;
            blocks[coordsToInt(12, 49, 15)] = (byte) 155;
            blocks[coordsToInt(13, 49, 15)] = (byte) 155;
            blocks[coordsToInt(14, 49, 15)] = (byte) 155;
            blocks[coordsToInt(15, 49, 15)] = (byte) 155;

            // alsó kerítés
            blocks[coordsToInt(1, 50, 14)] = (byte) 44;
            blocks[coordsToInt(2, 50, 14)] = (byte) 44;
            blocks[coordsToInt(3, 50, 14)] = (byte) 44;
            blocks[coordsToInt(4, 50, 14)] = (byte) 44;
            blocks[coordsToInt(5, 50, 14)] = (byte) 44;
            blocks[coordsToInt(6, 50, 14)] = (byte) 44;
            blocks[coordsToInt(7, 50, 14)] = (byte) 44;
            blocks[coordsToInt(8, 50, 14)] = (byte) 44;
            blocks[coordsToInt(9, 50, 14)] = (byte) 44;
            blocks[coordsToInt(10, 50, 14)] = (byte) 44;
            blocks[coordsToInt(11, 50, 14)] = (byte) 44;
            blocks[coordsToInt(12, 50, 14)] = (byte) 44;
            blocks[coordsToInt(13, 50, 14)] = (byte) 44;
            blocks[coordsToInt(14, 50, 14)] = (byte) 44;
            blocks[coordsToInt(15, 50, 14)] = (byte) 44;
        }

    }

    private class Plot {

        private final int x, z;
        private final OfflinePlayer owner;

        public Plot(int x, int z, OfflinePlayer owner) {
            this.owner = owner;
            this.x = x;
            this.z = z;
        }

        private void placeSign() {
            Block block = Bukkit.getWorld("plotworld").getBlockAt(x * 32 + 30, 50, z * 32 + 31);
            Sign sign = (Sign) block.getState();
            sign.setLine(0, "Plot " + x + ", " + z);
            sign.setLine(1, owner.getName());
            sign.update();
        }
    }
}
