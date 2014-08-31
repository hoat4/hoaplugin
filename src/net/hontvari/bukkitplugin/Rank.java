/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import org.bukkit.ChatColor;
import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.DARK_AQUA;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.YELLOW;

/**
 *
 * @author attila
 */
public enum Rank {

    PLAYER(YELLOW), VIP(GREEN), COOL(AQUA), HERO(GOLD), KING(DARK_RED), 
    BUILDER(YELLOW), MODERATOR(DARK_GRAY), ADMIN(DARK_AQUA), OWNER(DARK_GREEN);
    
    private final ChatColor color;

    private Rank(ChatColor color) {
        this.color = color;
    }

    public String colorForJSON() {
        return color.name().toLowerCase();
    }

}
