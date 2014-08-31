/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hontvari.bukkitplugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author attila
 */
public class JumpGen {
    private Player sender;
    private static final int[][] PossibleJumps ={{4,4,4,3}, {5,5,5,4,4}, {5,5,5,5,4},{6,6,5,5,5},{6,6,6,6,5,5}, {6,6,6,6,6,5},{7,7,7,6,6,5}};
    public JumpGen(Player sender) {
        this.sender = sender;
    }
    
}
