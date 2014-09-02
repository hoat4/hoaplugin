/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author attila
 */
public class World {

    private final org.bukkit.World world;

    public static World get(String name) {
        return new World(Bukkit.getWorld(name));
    }

    static World of(org.bukkit.World world) {
        return new World(world);
    }

    static World get(CommandSender sender) {
        return of(sender instanceof BlockCommandSender ? ((BlockCommandSender) sender).getBlock().getWorld() : ((Player) sender).getWorld());
    }
    public final String name;

    private World(org.bukkit.World world) {
        this.world = world;
        name = world.getName();
    }

    public Block block(int x, int y, int z) {
        return new Block(world.getBlockAt(x, y, z));
    }

    public Block block(Location l) {
        return block(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static class Block {

        static Block of(org.bukkit.block.Block block) {
            return new Block(block);
        }

        private final org.bukkit.block.Block block;
        private Material material;
        public final int x, y, z;
        public final Location location;

        Block(org.bukkit.block.Block block) {
            this.block = block;
            material = Material.get(block.getTypeId());
            x = block.getX();
            y = block.getY();
            z = block.getZ();
            location = block.getLocation();
        }

        public void setMaterial(Material material) {
            if (material.equals(this.material))
                return;
            block.setTypeId(material.getID());
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }

        public void clear() {
            setMaterial(Material.AIR);
        }

        public boolean is(Material material) {
            return this.material.equals(material);
        }

        public boolean is(String material) {
            System.out.println(this.material +" == "+Material.get(material));
            return this.material.equals(Material.get(material));
        }

        public boolean isNot(Material material) {
            return !is(material);
        }

        public boolean isNot(String material) {
            return !is(material);
        }

        public Block up(int i) {
            return of(block.getRelative(0, i, 0));
        }

        public Block down(int i) {
            return of(block.getRelative(0, -i, 0));
        }

        public <T extends BlockState> T getContent(Class<T> aClass) {
            return (T) block.getState();
        }
    }
}
