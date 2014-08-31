/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.StringJoiner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author attila
 */
class GraphiteWriter implements Runnable {
    private final HoaBukkitPlugin plugin;

    private final BufferedWriter graphiteOut;
    private int gl;

    public GraphiteWriter(HoaBukkitPlugin plugin, BufferedWriter graphiteOut) {
        this.plugin = plugin;
        this.graphiteOut = graphiteOut;
    }

    @Override
    public void run() {
        if(!plugin.isEnabled)
            return;
        try {
            StringBuilder joiner = new StringBuilder("receivePlayers([").append(+Bukkit.getOnlinePlayers().length);
            for (Player player : Bukkit.getOnlinePlayers())
                joiner.append(",\"").append(player.getName()).append("\"");
            joiner.append("]);\nif(typeof players === undefined)\n    window.players = new Object()\n");
            for (Player player : Bukkit.getOnlinePlayers()) {
                joiner.append("players['").append(player.getName()).append("'] = {gamemode: '").append(player.getGameMode()).append("', address: '").append(player.getAddress()).append("', world: '").append(player.getLocation().getWorld().getName()).append("', firstPlayed: ").append(player.getFirstPlayed()).append(", op: ").append(player.isOp()).append(", dead: ").append(player.isDead()).append("}\n");
            }
            new URL("http://attila.hontvari.net/place/actions/writeplayers?val=" + URLEncoder.encode(joiner.toString())).openStream().close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if ((++gl) % 10 == 0)
            try {
                writeProperty("playerCount", Bukkit.getOnlinePlayers().length);
                writeProperty("tps", Lag.getTPS());
                flush();
                //    System.out.println("elküldve:" + line);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
    }

    public void writeProperty(String name, double value) throws IOException {
        String line = "mc." + HoaBukkitPlugin.getServerName() + ".hoaplugin." + name + " " + value + " " + (System.currentTimeMillis() / 1000);
        graphiteOut.write(line);
        graphiteOut.newLine();
    }

    public void flush() throws IOException {
        graphiteOut.flush();
    }
}
