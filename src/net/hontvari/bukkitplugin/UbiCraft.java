/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
/**
 *
 * @author attila
 */
public class UbiCraft {

    public static final String varosWorldName = "FLAT_GR1GRO48BR1_UClHP_TOCP_CREA";

    private boolean spleefRunning = true, zrRunning, maRunning;
    private final World world = Bukkit.getWorld("world");
    private final Random random = new Random();
    private final Map<UUID, ItemStack[]> previnv = new HashMap<>();
    final HoaBukkitPlugin plugin;

    public UbiCraft(HoaBukkitPlugin plugin) {
        this.plugin = plugin;
        plugin.scheduled.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (zrRunning)
                    Bukkit.getWorld(varosWorldName).spawnEntity(new Location(Bukkit.getWorld(varosWorldName), 9996.5, 61, 10010.5), EntityType.ZOMBIE);
            }
        }, 0, 40));

        if (Bukkit.getWorld(varosWorldName) == null)
            new WorldCreator(varosWorldName).createWorld();

        setupMobArena();
        setupAirPlane();
        //     setupCar();
    }

    public boolean isInSpawn(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20091 && l.getBlockX() > 20046 && l.getBlockZ() < 20095 && l.getBlockZ() > 20052;
    }

    public boolean isInSpleef(Location l) {
        if (l == null)
            return false;
        if (precheck(l))
            return false;
        return l.getBlockX() < 20095 && l.getBlockX() > 20053 && l.getBlockZ() < 19992 && l.getBlockZ() > 19959;
    }

    public boolean isInAllatfarm(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20113 && l.getBlockX() > 20095 && l.getBlockZ() < 19992 && l.getBlockZ() > 19958;
    }

    public boolean isInMaradjtalpon(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20113 && l.getBlockX() > 20095 && l.getBlockZ() < 19992 && l.getBlockZ() > 19958;
    }

    public boolean isInFarm(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 19992 && l.getBlockX() > 19970 && l.getBlockZ() < 20047 && l.getBlockZ() > 20022;
    }

    boolean isInZombieRun(Location l) {
        if (precheck2(l))
            return false;
        return l.getBlockX() >= 9974 && l.getBlockZ() >= 10001 && l.getBlockX() <= 9998 && l.getBlockZ() <= 10018;
    }

    private boolean isInZombieRunLobby(Location l) {
        if (!l.getWorld().getName().equals(varosWorldName))
            return false;
        return l.getBlockX() >= -10 && l.getBlockZ() >= -19 && l.getBlockX() <= -7 && l.getBlockZ() <= -17;
    }

    private boolean isInMA(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20203 && l.getBlockX() > 20140 && l.getBlockZ() > 19979 && l.getBlockZ() < 20023;
    }
    public boolean isInPiac(Location l) {
        return l.getBlockX() >= -11 && l.getBlockZ() >= 1 && l.getBlockX() <= 8 && l.getBlockZ() <= 71;
    }

    public boolean isInTWOpenDetectionArea(Location l) {
        if (precheck(l, "plotworld"))
            return false;
        return l.getBlockX() <= 16 && l.getBlockX() >= 15 && l.getBlockZ() >= 17 && l.getBlockZ() <= 21;
    }

    public boolean blocksTWDoor(Entity entity) {
        return entity instanceof Player && isInTWOpenDetectionArea(entity.getLocation());
    }
    public boolean isInPVP(Location loc) {
        return loc.getX() < 20188 && loc.getX() > 20091 && loc.getZ() > 20097 && loc.getZ() < 20178;
    }
    
    public void setTelkekOpened(boolean value) {
        World plotworld = Bukkit.getWorld("plotworld");
        Material m = !value ? Material.STAINED_GLASS : Material.AIR;
        plotworld.getBlockAt(15, 53, 19).setType(m);
        plotworld.getBlockAt(15, 54, 19).setType(m);
        plotworld.getBlockAt(15, 55, 19).setType(m);
        plotworld.getBlockAt(16, 53, 19).setType(m);
        plotworld.getBlockAt(16, 54, 19).setType(m);
        plotworld.getBlockAt(16, 55, 19).setType(m);
    }

    public boolean isInMine20jump(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20162 && l.getBlockX() > 20140 && l.getBlockZ() < 19868 && l.getBlockZ() > 19844;
    }

    public boolean isInZsohJump(Location l) {
        if (precheck(l))
            return false;
        return l.getBlockX() < 20147 && l.getBlockX() > 20101 && l.getBlockZ() < 19938 && l.getBlockZ() > 19904;
    }

    public boolean canTeleportTo(PlayerTeleportEvent evt) {
        if (isInSpleef(evt.getTo()))
            return Warps.spleef.equals(evt.getTo()) && spleefRunning;
        if (isInZombieRun(evt.getTo()))
            if (evt.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)
                return true;
            else
                return false;
        if (isInMA(evt.getFrom())) {
            System.out.println("fromma");
            restoreInv(evt.getPlayer());
        }
        if (isInMA(evt.getTo())) {
            System.out.println("toma");
            saveInv(evt.getPlayer());
            PlayerInventory p = evt.getPlayer().getInventory();
            p.clear();
            p.addItem(new ItemStack(Material.DIAMOND_SWORD));
            p.addItem(new ItemStack(Material.DIAMOND_BOOTS));
        }

        return true;
    }

    public void startZR() {
        zrRunning = true;
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers())
            if (player.getWorld().getName().equals(varosWorldName) && isInZombieRunLobby(player.getLocation())) {
                player.teleport(new Location(player.getWorld(), 9978, 61, 10005 + random.nextInt(10), -190, 0));
                saveInv(player);
                player.setGameMode(GameMode.ADVENTURE);
                count++;
            }

        chat(String.format(plugin.i18n("minigame.zr.started"), count));
        //Bukkit.broadcastMessage("zr beginnelve "+count+" emberkével. ");
    }

    public void saveInv(Player player) {
        ItemStack[] inv = new ItemStack[9 * 4];
        int i = 0;
        for (ItemStack itemStack : player.getInventory())
            inv[i++] = itemStack;
        previnv.put(player.getUniqueId(), inv);
        System.out.println("saved inv for " + player.getUniqueId());
    }

    public void stopZR() {
        zrRunning = false;
        for (Entity entity : world.getEntities())
            if (entity.getType() == EntityType.ZOMBIE && isInZombieRun(entity.getLocation()))
                entity.remove();
        chat("minigame.zr.end");
    }

    public void startSpleef() {
        spleefRunning = true;
    }

    private boolean precheck(Location l) {
        return !l.getWorld().getName().equals("world");
    }

    public void zrLoss(Player player) {
        player.teleport(Warps.zombierun);
        restoreInv(player);
        Player winner = null;
        int count = 0;
        for (Player otherPlayer : Bukkit.getOnlinePlayers())
            if (otherPlayer.getWorld().getName().equals(varosWorldName) && isInZombieRun(otherPlayer.getLocation())) {
                winner = otherPlayer;
                count++;
            }
        if (count == 1) {
            chat(String.format(plugin.i18n("minigame.zr.winner"), winner.getName()));
            winner.teleport(Warps.zombierun);
            restoreInv(winner);
            stopZR();
        }
    }

    public void restoreInv(Player player) throws IllegalArgumentException {
        if (!previnv.containsKey(player.getUniqueId())) {
            player.getInventory().clear();
            ItemStack[] inv = previnv.get(player.getUniqueId());
            for (ItemStack itemStack : inv)
                if (itemStack != null)
                    player.getInventory().addItem(itemStack);
            System.out.println(player.getUniqueId() + " inv restored");
        } else
            System.out.println("noprevinv for " + player.getUniqueId());
    }

    public void startMA() {
        chat("minigame.ma.start");
        maRunning = true;
    }

    public void stopMA() {
        chat("minigame.ma.stop");
        maRunning = false;
        for (Entity entity : world.getEntities())
            if (!(entity instanceof Player) && isInMA(entity.getLocation()))
                entity.remove();
    }

    private void setupMobArena() {
        /*Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

         @Override
         public void run() {
         world.spawnEntity(new Location(world, 20194, 63, 20021), EntityType.IRON_GOLEM);
         }
         }, 0, 200);*/

        int multiplier = 40;
        int fullDelay = 50 * multiplier;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20182, 63, 20021), EntityType.ZOMBIE);
            }
        }, 2 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20185, 63, 20021), EntityType.SPIDER);
            }
        }, 4 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20185, 63, 20021), EntityType.BLAZE);
            }
        }, 6 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20188, 63, 20021), EntityType.CAVE_SPIDER);
            }
        }, 8 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20191, 63, 20021), EntityType.CREEPER);
            }
        }, 10 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20191, 63, 20021), EntityType.MAGMA_CUBE);
            }
        }, 12 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20194, 63, 20021), EntityType.MAGMA_CUBE);
            }
        }, 14 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20197, 63, 20021), EntityType.ZOMBIE);
            }
        }, 16 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20200, 63, 20021), EntityType.ZOMBIE);
            }
        }, 18 * multiplier, fullDelay);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20201, 63, 20004), EntityType.SKELETON);
            }
        }, 20 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20181, 63, 20004), EntityType.SKELETON);
            }
        }, 22 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20181, 63, 20009), EntityType.SLIME);
            }
        }, 24 * multiplier, fullDelay);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20182, 63, 20021), EntityType.ZOMBIE);
            }
        }, 26 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20185, 63, 20021), EntityType.SPIDER);
            }
        }, 28 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20185, 63, 20021), EntityType.BLAZE);
            }
        }, 30 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20188, 63, 20021), EntityType.PIG_ZOMBIE);
            }
        }, 32 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20191, 63, 20021), EntityType.BLAZE);
            }
        }, 34 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20191, 63, 20021), EntityType.ZOMBIE);
            }
        }, 36 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20194, 63, 20021), EntityType.MAGMA_CUBE);
            }
        }, 38 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20197, 63, 20021), EntityType.MAGMA_CUBE);
            }
        }, 40 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20200, 63, 20021), EntityType.ZOMBIE);
            }
        }, 42 * multiplier, fullDelay);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20201, 63, 20004), EntityType.SPIDER);
            }
        }, 44 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20181, 63, 20004), EntityType.SKELETON);
            }
        }, 46 * multiplier, fullDelay);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (maRunning)
                    world.spawnEntity(new Location(world, 20181, 63, 20009), EntityType.SLIME);
            }
        }, 48 * multiplier, fullDelay);
    }

    public String getWorldDisplayName(World world) {
        String result = plugin.i18n("world." + world.getName());
        if (result == null)
            return plugin.i18n("world.unknown");
        return result;
    }

    private boolean precheck(Location l, String worldName) {
        return !l.getWorld().getName().equals(worldName);
    }

    private void setupAirPlane() {
        World world = Bukkit.getWorld(varosWorldName);
        final Block[] lamps = {
            world.getBlockAt(15, 110, -33),
            world.getBlockAt(-1, 102, -15),
            world.getBlockAt(-1, 102, -51),
            world.getBlockAt(-18, 100, -33), //
        };
        //final Material off = Material.REDSTONE_LAMP_OFF;
        final Material on = Material.REDSTONE_LAMP_ON;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (plugin.isEnabled)
                    for (Block lamp : lamps)
                        lamp.setType(on);
            }
        }, 0, 20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {
                if (plugin.isEnabled)
                    for (Block lamp : lamps)
                        lamp.setType(on);
            }
        }, 5, 20);
    }

    public void setupCar() {
        final World world = Bukkit.getWorld(varosWorldName);
        world.spawnEntity(new Location(world, 0, 50, 25), EntityType.PLAYER);
        /*Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
         private int progress = 25;

         @Override
         public void run() {
         if (plugin.isEnabled)

         fb.setVelocity(new Vector(0, 0.4, 0.5)); /*                    Block oldBlock = world.getBlockAt(0, 50, progress);
         Block newBlock = world.getBlockAt(0, 50, ++progress);

         oldBlock.setType(Material.AIR);
         newBlock.setType(Material.GLOWSTONE);*/
        /*  }
         }, 5, 4);*/
    }

    private boolean precheck2(Location l) {
        return !l.getWorld().getName().equals(varosWorldName);
    }
}
