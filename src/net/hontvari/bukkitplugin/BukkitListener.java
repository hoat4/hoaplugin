/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.InventoryToString;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.needLogin;
import net.minecraft.server.v1_7_R1.PacketPlayOutWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import static org.bukkit.Material.PISTON_BASE;
import static org.bukkit.Material.PISTON_EXTENSION;
import static org.bukkit.Material.PISTON_MOVING_PIECE;
import static org.bukkit.Material.PISTON_STICKY_BASE;
import static org.bukkit.Material.TNT;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Attila
 */
public class BukkitListener implements Listener {

    public static List<String> muted = new ArrayList<>();
    private final Server server;
    private final HoaBukkitPlugin plugin;

    private final UbiCraft uc;
    /* class PlayerChest{
     Player p; 
     Chest c;
     int id;

     public PlayerChest(Player p, Chest c, int id) {
     this.p = p;
     this.c = c;
     this.id = id;
     }
        
     }
     private final List<PlayerChest> cList = new ArrayList<>();
     */

    public BukkitListener(Server server, HoaBukkitPlugin plugin) {
        uc = plugin.uc;
        this.server = server;
        this.plugin = plugin;
        onStart();
    }

    @EventHandler
    public void on(BlockDispenseEvent evt) {
        int t = evt.getItem().getTypeId();
        boolean isSeed = t == 295 || t == 361 || t == 362 || t == 372;
        final Block b = evt.getBlock();
        if (isSeed && b.getTypeId() == 23) {
            Dispenser d = (Dispenser) b.getState();
            BlockFace f = new org.bukkit.material.Dispenser(27, b.getData()).getFacing();
            final Block r1 = b.getRelative(f);
            final Block r = r1.getRelative(BlockFace.DOWN);
            if (r.getTypeId() == 60 && r1.getTypeId() == 0) {
                evt.setCancelled(true);
                r1.setTypeId(1);
                System.out.println("planted");
            }
        }
        Location l = evt.getBlock().getLocation();
        if (l.getBlockX() == 20301 && l.getBlockY() == 254 && l.getBlockZ() == 20081) {
            Dispenser d = (Dispenser) b.getState();
            d.getInventory().addItem(new ItemStack(Material.MINECART));
        }
    }
    private final File spyDir = new File("hoa_spy");

    private void onStart() {
        if (!spyDir.exists())
            spyDir.mkdir();
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        if (event.isCancelled() || event.getPlayer() == null)
            return;
        if (event.getBlock().getType() == Material.SNOW_BLOCK && uc.isInSpleef(event.getBlock().getLocation()))
            event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL));
        Player p = event.getPlayer();
        if (p.getWorld().getName().equals("world"))
            if (isInFatelep(event.getBlock().getLocation()))
                if (event.getBlock().getType() == Material.GLOWSTONE || event.getBlock().getType() == Material.SANDSTONE)
                    event.setCancelled(true);

        if (needLogin.contains(p.getName())) {
            sendRegMessage(p);
            event.setCancelled(true);
        }
    }

    private void sendRegMessage(Player player) {
        if (HoaBukkitPlugin.logins.containsKey(player.getName()))
            chat(player, '6', "automatic.loginFirst");
        else
            chat(player, '4', "automatic.registerFirst");
    }

    @EventHandler
    public void on(EntityDamageEvent evt) {
        if (evt.getEntityType() == EntityType.PLAYER) {
            Location loc = evt.getEntity().getLocation();
            if (loc.getWorld().getName().equals("world")) {
                if (uc.isInZombieRun(loc))
                    uc.zrLoss((Player) evt.getEntity());
                if (evt.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && ((EntityDamageByEntityEvent) evt).getDamager() instanceof Player && !uc.isInPVP(loc)) {
                    System.out.println("dpvp:" + evt.getEntity().toString());
                    evt.setCancelled(true);
                }
            }
        }
    }

    private static boolean isInFatelep(Location location) {
        return location.getX() < 20037 && location.getX() > 19991 && location.getZ() < 20054 && location.getZ() > 20007;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        if (e.isCancelled())
            return;
        if (e.blockList().isEmpty())
            return;
        e.setYield(0F);

        Set<Material> disallowedBlocks = EnumSet.of(TNT, PISTON_BASE,
                PISTON_EXTENSION, PISTON_MOVING_PIECE, PISTON_STICKY_BASE);

        Location eLoc = e.getLocation();
        World w = eLoc.getWorld();
        List<Block> signs = new ArrayList<>();

        for (int i = 0; i < e.blockList().size(); i++) {
            Block b = e.blockList().get(i);
            Location l = b.getLocation();

            if (disallowedBlocks.contains(b.getType()))
                continue;
            if (b.getState() instanceof Sign)
                signs.add(b);

            b.getState();

            long delay = i * 4;
            Material material = b.getType();
            BlockState blockState = b.getState();

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    l.getBlock().setType(material);
                    blockState.update(true, false);

                    l.getWorld().getPlayers().stream().
                            filter((p) -> p.getLocation().distance(l) <= 1).
                            forEach((p) -> {
                                for (int y = 0;; y++) {
                                    boolean bottomIsAir = p.getLocation().clone().add(0, y, 0).getBlock().getType() == Material.AIR;
                                    boolean topIsAir = p.getLocation().clone().add(0, y + 1, 0).getBlock().getType() == Material.AIR;
                                    if (bottomIsAir && topIsAir) {
                                        p.teleport(p.getLocation().add(0, y, 0));
                                        break;
                                    }
                                }
                            });

                    l.getWorld().getPlayers().stream().
                            filter((p) -> (p.getLocation().distance(l) <= 10)).
                            forEach((p) -> {
                                if (new Random().nextInt(3) == 0)
                                    p.playSound(p.getLocation(), Sound.DIG_STONE, 10, 10);
                                PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(2001, l.getBlockX(), l.getBlockY(), l.getBlockZ(), material.getId(), false);
                                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                            });
                }
            }, delay);
        }

        e.blockList().removeAll(signs);
    }

    @EventHandler
    public void on(BlockPlaceEvent evt) {
        /*     if (evt.getBlock().getType() == Material.FENCE) {
         evt.getBlock().getWorld().getBlockAt(evt.getBlock().getX(), evt.getBlock().getY() + 1, evt.getBlock().getZ()).setType(Material.FENCE);
         evt.getBlock().getWorld().getBlockAt(evt.getBlock().getX(), evt.getBlock().getY() + 2, evt.getBlock().getZ()).setType(Material.FENCE);
         evt.getBlock().getWorld().getBlockAt(evt.getBlock().getX(), evt.getBlock().getY() + 3, evt.getBlock().getZ()).setType(Material.FENCE);
         }*/
        Player p = evt.getPlayer();
        if (needLogin.contains(p.getName())) {
            sendRegMessage(p);
            evt.setCancelled(true);
            return;
        }
        if (plugin.mode36.contains(evt.getPlayer().getUniqueId())) {
            evt.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, new Runnable() {

                @Override
                public void run() {
                    evt.getBlock().setTypeId(36);
                    Location l = evt.getBlock().getLocation().clone();
                    //l.setY(l.getY() + 1);
                    l.getWorld().spawnFallingBlock(l, evt.getItemInHand().getType(), evt.getItemInHand().getData().getData());
                }
            });
        }
        if (plugin.lampbuilders.contains(p.getName())) {
            System.out.println("Creating lamp...");
            evt.getBlock().getRelative(0, 1, 0).setTypeId(36);
            evt.getBlock().getWorld().spawnFallingBlock(evt.getBlock().getRelative(0, 2, 0).getLocation(), 124, (byte) 0);
        }
    }

    @EventHandler
    public void on(CreatureSpawnEvent evt) {
        if (evt.getEntity().getWorld().getName().equals("plotworld"))
            switch (evt.getEntityType()) {
                case CREEPER:
                case ENDERMAN:
                case ZOMBIE:
                case SKELETON:
                case SPIDER:
                case SLIME:
                case WITCH:
                    evt.setCancelled(true);
            }
    }

    @EventHandler
    public void on(PlayerInteractEntityEvent evt) {
        Block b = evt.getRightClicked().getLocation().getBlock();
        if (b.getTypeId() == 36) {
            b.setType(Material.AIR);
            evt.getRightClicked().remove();
        }
    }

    @EventHandler
    public void on(WeatherChangeEvent evt) {
        if (evt.getWorld().getName().equals("SkyPvP"))
            evt.setCancelled(true);
    }

    @EventHandler
    public void on(InventoryOpenEvent evt) {
        if (evt.getInventory().getTitle().equals("Kiegészítő végzetláda") && evt.getPlayer().equals(evt.getInventory().getHolder()))
            plugin.hoadata.setProperty("extension_enderchest." + evt.getPlayer().getUniqueId(), InventoryToString(evt.getInventory()));
        if (plugin.uc.isInPiac(evt.getPlayer().getLocation()) && evt.getInventory().getHolder() instanceof Chest)
            try {
                int price = getPrice(evt.getInventory());
                int money = plugin.getmoney((Player) evt.getPlayer());
                if (money < price) {
                    evt.setCancelled(true);
                    chat(evt.getPlayer().getName(), "Kevés a money. ");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

    }

    @EventHandler
    public void on(InventoryClickEvent evt) {
        if (plugin.uc.isInPiac(evt.getWhoClicked().getLocation()) && evt.getInventory().getHolder() instanceof Chest)
            if (evt.getAction() != InventoryAction.NOTHING && !evt.getAction().name().startsWith("PICKUP"))
                if ((evt.getRawSlot() > 26 || evt.getRawSlot() == -999))
                    try {
                        int price = getPrice(evt.getInventory());
                        OfflinePlayer owner = getMarketChestOwner(evt.getInventory());
                        int money = plugin.getmoney((Player) evt.getWhoClicked());
                        money -= price;
                        plugin.setmoney((Player) evt.getWhoClicked(), money);
                        if (money < price) {
                            evt.setCancelled(true);
                            evt.setResult(Event.Result.DENY);
                            evt.setCursor(new ItemStack(Material.AIR));
                            ((Player) evt.getWhoClicked()).updateInventory();
                            evt.getWhoClicked().getOpenInventory().close();
                        }
                        System.out.println(price);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
    }

    @EventHandler
    public void on(InventoryCloseEvent evt) {
        if (evt.getInventory().getTitle().equals("Kiegészítő végzetláda") && evt.getPlayer().equals(evt.getInventory().getHolder()))
            plugin.hoadata.setProperty("extension_enderchest." + evt.getPlayer().getUniqueId(), InventoryToString(evt.getInventory()));
    }

    private int getPrice(Inventory inv) {
        String title = inv.getTitle();
        System.out.println(title);
        if (title.startsWith("§6Ár: ") && title.endsWith(" UbiCoin / db")) {
            int price = Integer.decode(title.substring("§6Ár: ".length(), title.length() - " UbiCoin / db".length()));
            return price;
        }
        return -1;
    }

    private OfflinePlayer getMarketChestOwner(Inventory inv) {
        String title = inv.getTitle();
        System.out.println(title);
        String name = title.substring(title.indexOf('X') + 1);
        return Bukkit.getOfflinePlayer(name);
    }
}
