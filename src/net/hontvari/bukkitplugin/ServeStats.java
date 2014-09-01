/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
import net.minecraft.server.v1_7_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author attila
 */
public class ServeStats {

    public static final String sb_name = "Szever infó";
    public static final String PC_SCORE = "Player Count";
    public static final String FREERAM_TITLE = "Free RAM in KB";
    public static final String TPS_TITLE = "TPS";
    public static final String ENTITYCOUNT_TITLE = "Entity Count";

    private static void sendCrashPacket(Player player) {
        /*   Scoreboard sb = new Scoreboard();//Create new scoreboard
         sb.registerObjective("crash", new ScoreboardBaseCriteria("crash"));//Create new objective in the scoreboard

         Packet206SetScoreboardObjective packet = new Packet206SetScoreboardObjective(sb.getObjective("crash"), 0);//Create Scoreboard create packet
         Packet208SetScoreboardDisplayObjective display = new Packet208SetScoreboardDisplayObjective(1, sb.getObjective("crash"));//Create display packet set to sidebar mode

         sendPacket(player, packet);//Send Scoreboard create packet
         sendPacket(player, display);//Send the display packet*/
    }

    List<Player> players = new CopyOnWriteArrayList<>();
    // Map<Player, Scoreboard> map = new ConcurrentHashMap<>();
    HoaBukkitPlugin plugin;

    public ServeStats(HoaBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void onDisable() {
        for (Player player : players)
            removeStats(player);
        players.clear();
    }

    public void onEnable() {
        plugin.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                Bukkit.getScheduler().runTask(plugin, ServeStats.this::update);
            }
        }, 0, 50);
    }

    public void remove(Player player) {
        if (players.contains(player)) {
            players.remove(player);
            removeStats(player);
            chat(player, "Már nem látod a szerver statisztikákat.");
        } else
            chat(player, "Eddig se láttad a szever statisztikákat.");
    }

    void add(Player player) {
        if (players.contains(player)) {
            chat(player, "Már most is látod a szerver statisztikát.");
            return;
        }
        players.add(player);

        /* Scoreboard sb = new Scoreboard();//Create new scoreboard
         sb.registerObjective(sb_name, new ScoreboardBaseCriteria(sb_name));//Create new objective in the scoreboard

         Packet206SetScoreboardObjective packet = new Packet206SetScoreboardObjective(sb.getObjective(sb_name), 0);//Create Scoreboard create packet
         Packet208SetScoreboardDisplayObjective display = new Packet208SetScoreboardDisplayObjective(1, sb.getObjective(sb_name));//Create display packet set to sidebar mode
         if (!noscore.contains(player)) {
         sendPacket(player, packet);//Send Scoreboard create packet
         }
         sendPacket(player, display);//Send the display packet

         map.put(player, sb);
         */
        chat(player, "[HoaPlugin] Kész");
    }

    public static void sendPacket(Player player, Packet packet) {
//        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
    public static Set<Player> noscore = new HashSet<>();

    private void removeStats(Player player) {
        System.out.println("removestats");
        players.remove(player);
        /* Scoreboard sb = map.get(player);
         Packet206SetScoreboardObjective packet = new Packet206SetScoreboardObjective(sb.getObjective(sb_name), 1);
         sendPacket(player, packet);
         rmData(PC_SCORE, sb, player);
         rmData(FREERAM_TITLE, sb, player);
         rmData(TPS_TITLE, sb, player);
         rmData(ENTITYCOUNT_TITLE, sb, player);
         noscore.add(player);
         map.remove(player);*/
    }
    private int num;

    public void update() {
        // System.out.println("update1");
        int free = (int) ((Runtime.getRuntime().freeMemory()) / 1000);
        int pc = Bukkit.getOnlinePlayers().length;
        int entityCount = countEntities();
        int tps = (int) Math.round(Lag.getTPS()*100);
        Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Server.MainStats").getScore(Bukkit.getOfflinePlayer("TPS")).setScore(tps);
        Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Server.MainStats").getScore(Bukkit.getOfflinePlayer("Player Count")).setScore(pc);
        Bukkit.getScoreboardManager().getMainScoreboard().getObjective("Server.MainStats").getScore(Bukkit.getOfflinePlayer("Entity Count")).setScore(entityCount);
    }

    public static void crashClient(Player player) {
        sendCrashPacket(player);
        sendCrashPacket(player);

    }

    public static int countEntities() {
        int result = 0;
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds)
            result += world.getEntities().size();
        return result;
    }
}
