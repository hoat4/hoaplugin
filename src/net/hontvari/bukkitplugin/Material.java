/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import hoat4.place.client.api.Uploaded;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author attila
 */
public class Material {

    private static Map<String, Material> ids = new HashMap();
    private static Map<Integer, Material> idsByIdInt = new HashMap();
    private static Map<String, Material> idsByName = new HashMap();
    public static final Material AIR;

    static {
        try {
            String[] lines = Uploaded.byName("51zi").stringContent().split("\n");
            for (int i = 0; i < lines.length; i += 3) {
                Material idd = new Material(lines[i].trim(), lines[i + 1], lines[i + 2], 0, null);
                ids.put(idd.id, idd);
                System.out.println(idd);
                idsByName.put(idd.name, idd);
                idsByIdInt.put(idd.idInt, idd);
            }
            AIR = get(0);
            System.out.println("done");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String displayName;
    public String name;
    final String id;
    private Material[] withData = new Material[16];
    private final int data, idInt;

    private Material(String id, String string, String string0, int data, Material[] withData) {
        displayName = string.substring(0, string.length() - 1);
        name = string0.substring(1, string0.length() - 2);
        this.id = id;
        if (id.contains(":")) {
            data = Integer.decode(id.substring(id.indexOf(':') + 1));
            id = id.substring(0, id.indexOf(':'));
        }
        if (withData == null) {
            withData = new Material[16];
            for (int i = 0; i < 16; i++)
                withData[i] = new Material(id, string, string0, i, withData);
        }
        this.withData = withData;
        this.data = data;
        idInt = Integer.decode(id);
    }

    @Override
    public String toString() {
        return id + ": " + name + " = " + displayName;
    }

    public static Material get(String name) {
        if (name.startsWith("minecraft:"))
            name = name.substring("minecraft:".length());
        if (name.contains(":")) {
            String[] split = name.split(":");
            return get(split[0]).withData[Integer.decode(split[1])];
        }
        Material intGet = ids.get(name);
        if (intGet != null)
            return intGet;
        Material result = idsByName.get("minecraft:" + name);
        if (result == null) {
            System.err.println("Material not found: " + name);
            System.out.println(idsByName);
        }
        return result;
    }

    public static Material get(int id) {
        return idsByIdInt.get(id);
    }

    public ItemStack createStack(int i) {
        return new ItemStack(idInt, i, (short) data, (byte) data);
    }

    int getID() {
        return idInt;
    }

    @Override
    public int hashCode() {
        return data << 24 | idInt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return obj.hashCode() == hashCode();
    }

    public Material withData(int data) {
        if (data < 0)
            return this;
        return withData[data];
    }

    public ItemStack createStack() {
        return createStack(1);
    }
}
