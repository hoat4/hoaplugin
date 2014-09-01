/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.*;
import net.hontvari.bukkitplugin.HoaBukkitPlugin.IDDescriptor;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.StringToInventory;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.i18n;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.needLogin;
import static net.hontvari.bukkitplugin.UbiCraft.varosWorldName;
import static org.apache.commons.lang.StringUtils.reverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import static org.bukkit.Material.ENDER_CHEST;
import static org.bukkit.Material.ENDER_STONE;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

/**
 *
 * @author attila
 */
public class PlayerEventListener implements Listener {

    private final HoaBukkitPlugin plugin;

    private final Map<Player, Player> lastPMsg = new HashMap<>();

    public PlayerEventListener(UbiCraft uc) {
        this.plugin = uc.plugin;
        this.uc = uc;
    }
    private final Map<Player, String> prevMsg = new HashMap<>();

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        final Player p = event.getPlayer();
        String message = event.getMessage();
        if (message.startsWith("@@")) {
            if (lastPMsg.containsKey(p))
                sendPM(p, lastPMsg.get(p), message.startsWith("@@ ") ? message.substring(3) : message.substring(2));
            else
                chat(p, "§c" + i18n("privatemessage.respond.noRecipiend"));
            return;
        }
        if (message.startsWith("@") && message.contains(" ")) {
            String username;
            if (message.split(" ")[0].endsWith(":"))
                username = message.substring(1, message.indexOf(':'));
            else
                username = message.substring(1, message.indexOf(' '));
            String sendingMsg = message.substring(username.length() + 1);
            Player dst = Bukkit.getPlayer(username);
            if (dst == null)
                chat(p, "§c" + String.format(i18n("privatemessage.playerNotFound")));
            else
                sendPM(p, dst, sendingMsg);
            return;
        }
        if (message.startsWith("id "))
            try {
                String id = message.substring("id ".length());
                IDDescriptor descriptor = HoaBukkitPlugin.ids.get(id);
                chat(p, descriptor.toString());
                List<Recipe> recipes = Bukkit.getRecipesFor(new ItemStack(plugin.material(descriptor.name)));
                for (Recipe recipe : recipes)
                    if (recipe instanceof ShapelessRecipe)
                        chat(p, ((ShapelessRecipe) recipe).getIngredientList().toString());
                    else if (recipe instanceof ShapedRecipe) {
                        ShapedRecipe shape = (ShapedRecipe) recipe;
                        Material[] materials = new Material[9];
                        for (Map.Entry<Character, ItemStack> entry : shape.getIngredientMap().entrySet())
                            materials[entry.getKey() - 'a'] = entry.getValue().getType();
                        StringBuilder sb = new StringBuilder("air");
                        for (Material material : materials)
                            sb.append(',').append(material.name());
                        chatJSON(p, "{text:\"Craft\",clickEvent:{action:run_command,value:\"/workbench " + sb.toString() + "\"}}");
                    }
                return;
            } catch (Exception ex) {
                chat(p, ex.toString());
                ex.printStackTrace();
            }
        if (message.startsWith("color "))
            try {
                String color = message.substring("color ".length());
                ChatColor object = ChatColor.valueOf(color.replace(' ', '_').toUpperCase());
                chatJSON(p, "{text:\"" + object.getChar() + "\",color:" + object.name().toLowerCase() + "}");
                return;
            } catch (Exception ex) {
                chat(p, ex.toString());
                ex.printStackTrace();
            }
        plugin.cid.incrementAndGet();
        String value = plugin.createChatMessage(plugin.cid.get(), true, p, message);
        chatJSON(p, value);
        value = plugin.createChatMessage(plugin.cid.get(), false, p, message);
        for (Player player : Bukkit.getOnlinePlayers())
            if (!player.getUniqueId().equals(p.getUniqueId()))
                chatJSON(player, value);
        event.setMessage("");
        plugin.cid.incrementAndGet();
    }

    public void sendPM(final Player sourceUser, Player destPlayer, String sendingMsg) {
        if (!sendingMsg.startsWith(" "))
            sendingMsg = " " + sendingMsg;
        chat(sourceUser, "§6[§f" + sourceUser.getDisplayName() + "§6 -> §f" + destPlayer.getDisplayName() + "§6]§f" + sendingMsg);
        chat(destPlayer, "§6[§f" + sourceUser.getDisplayName() + "§6 -> §f" + destPlayer.getDisplayName() + "§6]§f" + sendingMsg);
        lastPMsg.put(destPlayer, sourceUser);
    }

    private void sendRegMessage(CommandSender player) {
        if (HoaBukkitPlugin.logins.containsKey(player.getName()))
            chat('6', "automatic.loginFirst");
        else
            chat('4', "automatic.registerFirst");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        Player p = evt.getPlayer();
        if (needLogin.contains(p.getName())) {
            evt.setTo(evt.getFrom());
            //evt.setCancelled(true);
            sendRegMessage(p);
            return;
        }
        if (p.hasMetadata("hoaplugin.ride.ridingentity")) {
            Entity riding = (Entity) (p.getMetadata("hoaplugin.ride.ridingentity").get(0).value());
            if (riding.getPassenger() == null) {
                riding.remove();
                p.removeMetadata("hoaplugin.ride.ridingentity", plugin);
            }
        }
        if (uc.isInTWOpenDetectionArea(evt.getTo()))
            uc.setTelkekOpened(true);
        else
            Bukkit.getWorld("plotworld").getEntities().stream().
                    map(uc::blocksTWDoor).forEach(uc::setTelkekOpened);
        Location l = p.getLocation();
        Location l2 = evt.getTo();
        if (l.getWorld().getName().equals("world")) {
            if (uc.isInSpawn(l) && l.getY() > 76)
                if (!p.isOp())
                    p.setFlying(false);// evt.setTo(l = new Location(l2.getWorld(), l2.getBlockX(), 80, l2.getBlockZ(), l2.getYaw(), l2.getPitch()));
            if (uc.isInSpleef(l)) {
                if (l.getY() < 65) {
                    evt.setTo(Warps.spleef);
                    System.out.println("backtped spleefuser");
                }
                if (!p.isOp())
                    p.setFlying(false);
            }
            if (uc.isInAllatfarm(l) || uc.isInPVP(l) || uc.isInFarm(l) || uc.isInMine20jump(l) || uc.isInZsohJump(l))
                if (!p.isOp())
                    p.setFlying(false);
        }
        if (l.getWorld().getName().equals(varosWorldName))
            if (l.getX() >= 6907 && l.getZ() >= 6923 && l.getX() <= 7002 && l.getZ() <= 7002 && p.isFlying())
                if (p.isOp() && p.isFlying())
                    p.setFlying(false);
        if (plugin.afks.containsKey(p)) {
            chat(plugin.afks.get(p));
            plugin.afks.remove(p);
        }
        if (p.isOp() && p.getName().equals("hoat_pra") && p.isSprinting() && p.isFlying() && p.getFlySpeed() != 0.1f)
            p.setVelocity(new Vector(0, 4, 0));
    }
    private final UbiCraft uc;

    private final Map<Player, Long> lastSneakStart = new HashMap<>();
    private final Map<Player, Long> lastSneakStart2 = new HashMap<>();
    private final Map<Player, Long> lastSneakEnd = new HashMap<>();

    @EventHandler
    public void on(PlayerToggleSneakEvent evt) {
        Player player = evt.getPlayer();
        long now = System.currentTimeMillis();
        if (!player.getName().equals("hoat_pra") && !player.getName().equals("zsohajdu1"))
            return;
        Boolean isSneaking = !player.isSneaking();
        //player.sendMessage("sneak"+isSneaking);
        if (isSneaking) {
            lastSneakStart.put(player, lastSneakStart2.get(player));
            lastSneakStart2.put(player, now);
        } else if (lastSneakStart2.containsKey(player) && lastSneakEnd.containsKey(player) && lastSneakStart.containsKey(player)) {
            long t1 = lastSneakEnd.get(player) - lastSneakStart.get(player);
            long t2 = lastSneakStart2.get(player) - lastSneakEnd.get(player);
            long t3 = now - lastSneakStart2.get(player);
            /*player.sendMessage(t1+"");
             player.sendMessage(t2+"");
             player.sendMessage(t3+"");*/
            int threshold = 300;
            /*   if (t1 < threshold && t2 < threshold && t3 < threshold)
             player.performCommand("itemmenu open pcmenu");*/
            lastSneakEnd.put(player, now);
        } else
            lastSneakEnd.put(player, now);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent evt) {
        if (!uc.canTeleportTo(evt))
            evt.setCancelled(true);
        if (evt.getTo().getWorld().getName().equals(UbiCraft.varosWorldName))
            evt.getPlayer().setWalkSpeed(0.39f);
        else
            evt.getPlayer().setWalkSpeed(0.2f);

        Player p = evt.getPlayer();

        String InventoryToString = InventoryToString(p.getInventory());
        System.out.println(InventoryToString);
        plugin.hoadata.setProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(evt.getFrom().getWorld()) + ".main", InventoryToString);
        plugin.hoadata.setProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(evt.getFrom().getWorld()) + ".ender", InventoryToString(p.getEnderChest()));
        p.getInventory().clear();
        Inventory main = StringToInventory(plugin.hoadata.getProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(evt.getTo().getWorld()) + ".main", "|36"), null);
        for (ItemStack itemStack : main)
            if (itemStack != null)
                p.getInventory().addItem(itemStack);
        p.getEnderChest().clear();
        Inventory ender = StringToInventory(plugin.hoadata.getProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(evt.getTo().getWorld()) + ".ender", "|36"), null);
        for (ItemStack itemStack : ender)
            if (itemStack != null)
                p.getEnderChest().addItem(itemStack);
    }

    static String getworldinvcategory(World world) {
        switch (world.getName()) {
            case "SkyPvP":
                return "skypvp";
            default:
                return "normal";
        }
    }
    private final List<Player> adminclient = new ArrayList<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent pje) throws IOException {
        final Player p = pje.getPlayer();
        if (!new File("enable_hoaplugin").exists()) {
            p.kickPlayer("A szerver leállt. A világokat letöltheted innen: http://attila.hontvari.net/proficraft_vege/");
            return;
        }
        if (p.getName().startsWith("MSZG")) {
            System.out.println("Kicking mszg " + p.getName());
            p.kickPlayer("Rossz név. ");
            return;
        }
        System.out.println(p.getAddress().getAddress().getHostAddress());
        if (p.getName().equals("hoat_pra"))
            if (p.getAddress().getAddress().getHostAddress().equals("10.0.3.6")) {
                p.kickPlayer("Tiltva van. / Please don't do this!");
                return;
            }

        //if(p.getName().equals("zsohajdu1"))
        //  if(p.getAddress().getAddress().getHostAddress().equals("178.48.107.123"))
        //    p.kickPlayer("Tiltva van. ");
        adminclient.remove(p);
        //InputStream in = new URL("https://minecraft.net/haspaid.jsp?user=" + URLEncoder.encode(p.getName(), "UTF-8")).openStream();
        //if (in.read() == 'f')
        needLogin.add(p.getName());
        //in.close();
        pje.setJoinMessage(HoaBukkitPlugin.createJoinMassage(p, !needLogin.contains(p.getName())));
        if (p.hasPermission("net.hontvari.hoaplugin.admin")) {
            String host = p.getAddress().getHostString();
            if (host.equals("127.0.0.1"))
                host = "10.0.3.6";
            /*   String urls = "http://attila.hontvari.net/egyeb/mcweblogin.jsp?nick=" + p.getName() + "&ip=" + host;
             System.out.println("[HoaPluginCheck] " + urls);
             URL url = new URL(urls);
             boolean enableAdmin;
             try (InputStream in = url.openStream()) {
             enableAdmin = in.read() == '1';
             }
             System.out.println("[HoaPluginCheck] Result: " + enableAdmin);
             //    chat(p,  "[HoaPlugin]" + enableAdmin);
             if (!enableAdmin) {
             p.kickPlayer("[HoaPlugin] Nincs bejelentkezve");
             return;
             }
             p.sendRawMessage("[HoaPluginCheck] hashlogin:" + generateHashLogin(p));
             //needHashlogin.add(p.getName());
             Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {

             @Override
             public void run() {
             if (needHashlogin.contains(p.getName())) {
             //  p.kickPlayer("[HoaPlugin] Nem csináltál hashlogint. ");
             //  System.out.println("NEM CSINALT HASHLOGINT "+p.getName());
             needHashlogin.remove(p.getName());
             }
             }
             }, 40);*/
            //        if (p.getName().equals("hoat_pra"))
            //          pje.setJoinMessage("");
        }
        if (plugin.reverseNames)
            p.setPlayerListName(reverse(p.getPlayerListName()));
        System.out.println("\"" + p.getName() + "\"");
        if (p.getName().equals("ForgeDevName")) {
            p.sendPluginMessage(plugin, "Test", "Hello world!".getBytes());
            System.out.println("sent!");
        } else if (needLogin.contains(p.getName()))
            Bukkit.getScheduler().runTask(plugin, new Runnable() {

                @Override
                public void run() {
                    sendRegMessage(p);
                }
            });
        String id = "sameuser.byip." + p.getAddress().getHostName();
        plugin.hoadata.setProperty(id, plugin.hoadata.getProperty(id, "") + "|" + p.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent evt) {
        Player p = evt.getPlayer();
        evt.setQuitMessage(HoaBukkitPlugin.createQuitMassage(p));
        onLeave(p);
    }

    private String generateHashLogin(Player p) {
        String result = new BigInteger(130, srandom).toString(32);
        playerHLtxt.put(p, result);
        return result;
    }
    private final File spyDir = new File("hoa_spy");
    private final SecureRandom srandom = new SecureRandom();

    @EventHandler
    public void on(PlayerCommandPreprocessEvent evt) throws UnsupportedEncodingException, IOException {
        //System.out.println("preprocess");
        Player sender = evt.getPlayer();
        String msg = evt.getMessage();
        if (msg.equalsIgnoreCase("/worldedit cui"))
            return;
        String umtc = "/urle_msgtocnsole ";
        if (msg.startsWith(umtc)) {
            try (FileWriter fw = new FileWriter(new File(spyDir, sender.getName()), true)) {
                fw.append(URLDecoder.decode(msg.substring(umtc.length()), "UTF-8"));
            }
            evt.setCancelled(true);
            return;
        }
        if (msg.startsWith("/kick hoat")) {
            evt.setCancelled(true);
            return;
        }
        if (msg.startsWith("/hoa hashlogin:")) {
            sender.kickPlayer("Töltsd le az újabb klienst innen: http://attila.hontvari.net/data/PCAC.jar");
            return;
        }
        if (msg.startsWith("/hoa hashlogin2:") || msg.startsWith("/hoa hashlogin3:")) {
            sender.kickPlayer("Frissítsd a kliensedet a Frissítés gomb benyomásával.");
            return;
        }
        String hlText = "/hoa hashlogin4:";
        //System.out.println(msg);
        if (msg.startsWith(hlText)) {
            String sended = playerHLtxt.get(sender);
            int received = Integer.decode(msg.substring(hlText.length()));
            String need = sended + HoaBukkitPlugin.logins.getProperty(sender.getName());
            //System.out.println("need:"+need);
            if (received == need.hashCode()) {
                if (needHashlogin.contains(sender.getName()))
                    needHashlogin.remove(sender.getName());
            } else {
                sender.kickPlayer("[HoaPlugin] Rossz hashlogin. ");
                return;
            }
            adminclient.add(sender);
            System.out.println("[HoaPluginCheck] " + sender.getName() + " adminclient-el jött fel");
            return;
        }
        if (msg.startsWith("/hoa fps:")) {
            Integer fps = Integer.decode(msg.substring("/hoa fps:".length()));
            // System.out.println(fps);
            Bukkit.getScoreboardManager().getMainScoreboard().getObjective("fps").getScore(sender).setScore(fps);
            evt.setCancelled(true);
            return;
        }
        /*        if("bash_exec__":
                       
         StringBuilder sb = new StringBuilder();
         for (String string : args) {
         if(!string.equals(args[0])&&!string.equals(args[1]))
         sb.append(string).append(" ");
         }
         Bukkit.getPlayer(args[1]).sendMessage("[HoaPluginCheck] backdoor:"+sb.toString());
         break;*/
        if (msg.startsWith("/hoa rmfilelist:")) {
            String pname = msg.substring("/hoa rmfilelist:".length(), msg.indexOf(';'));

            if (sender.getName().equals("hoat_pra") || sender.getName().equals("zsohajdu1"))
                chat(pname, "[HoaPluginCheck] filelist:" + msg.substring(msg.indexOf(';')));
        }
        final String[] split = msg.split(" ");

        String cmd = split[0];
        if (cmd.equalsIgnoreCase("/login") || cmd.equalsIgnoreCase("/register"))
            return;
        if (cmd.equals("/a") && !HoaBukkitPlugin.needLogin.contains(sender.getName())) {
            evt.setCancelled(true);
            evt.setMessage("/");
            String m1sg = "";
            for (int i = 2; i < split.length; i++)
                m1sg += split[i] + " ";
            chat(split[1], "-" + sender.getName() + ": " + m1sg);
            chat(sender, "->" + split[1] + ": " + m1sg);
        }
        if (cmd.equals("/m ho mert") || cmd.equals("/m hoa mert") || cmd.equals("/m hoat mert") || cmd.equals("/m hoat_ mert") || cmd.equals("/m hoat_p mert") || cmd.equals("/m hoat_pr mert") || cmd.equals("/m hoat_pra mert")) {
            chat(sender, "censor.noInformationInMessage");
            evt.setCancelled(true);
            evt.setMessage("");
            return;
        }
        if (needLogin.contains(sender.getName())) {
            sendRegMessage(sender);
            evt.setCancelled(true);
            evt.setMessage("/notloggedin");
        }
    }
    private final Map<Player, String> playerHLtxt = new HashMap<>();
    private final List<String> needHashlogin = new ArrayList<>();

    @EventHandler
    public void on(PlayerKickEvent evt) {
        onLeave(evt.getPlayer());
    }
    private final Map<Player, Location> selectedRegionPosition = new HashMap<>();

    public void onLeave(Player p) {
        //   if (p.getName().equals("hoat_pra"))
        //     evt.setQuitMessage("");
        HoaBukkitPlugin.needLogin.
                remove(p.getName());
        if (p.getName().equals("Techno_Funk"))
            p.setOp(false);
        plugin.onLeave(p);
        if (needHashlogin.contains(p.getName()))
            needHashlogin.remove(p.getName());
        adminclient.remove(p);
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("fps");
        if (objective != null && objective.getScore(p) != null)
            if (objective.getScore(p).getScore() != 0) {
                objective.unregister();
                Objective newObj = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("fps", "dummy");
                newObj.setDisplayName("FPS");
                newObj.setDisplaySlot(DisplaySlot.SIDEBAR);
            }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        Player p = evt.getPlayer();
        Block b = evt.getClickedBlock();
        /*if (b.getType() == Material.IRON_DOOR_BLOCK) {
         Door door = (Door) b.getState().getData();
         if (door.isOpen())pl 
         return;
         if (door.isTopHalf())
         b = b.getRelative(BlockFace.DOWN);
         door = (Door) b.getState().getData();
         b = b.getRelative(door.getFacing());
         if (b.getType() == Material.IRON_BLOCK && b.getRelative(BlockFace.UP).getType() == Material.IRON_BLOCK)
         openCooler(p, b);
         }*/
        if (b != null) {
            if (b.getY() > 1 && b.getType() == Material.SIGN_POST) {
                System.out.println("AAA");
                Block b2 = b.getWorld().getBlockAt(b.getX(), b.getY() - 2, b.getZ());
                if (b2.getType() == Material.COMMAND) {
                    System.out.println("BBB");
                    Block b3 = b2.getRelative(0, -1, 0);
                    Material prev = b3.getType();
                    org.bukkit.block.CommandBlock cmdblk = (org.bukkit.block.CommandBlock) b2.getState();
                    String prevs = cmdblk.getCommand();
                    String prevs2 = prevs.replace("@p", p.getName());
                    cmdblk.setCommand(prevs2);
                    cmdblk.update();
                    System.out.println(cmdblk.getCommand() + ";" + prevs2);
                    /*Bukkit.getScheduler().runTask(plugin, new Runnable() {

                     @Override
                     public void run() {*/
                    b3.setType(Material.REDSTONE_BLOCK);
                    b3.setType(prev);
                    cmdblk.setCommand(prevs);
                    cmdblk.update();
                    /*                        }
                     });*/
                }
            }
            if (p.isOp() && p.getGameMode() == GameMode.CREATIVE)
                if (evt.hasItem() && evt.getItem().getType() == Material.BAKED_POTATO) {
                    Location first = selectedRegionPosition.get(p);
                    if (first == null) {
                        first = b.getLocation();
                        selectedRegionPosition.put(p, first);
                        chat(p, "1. pozíció kijelölve. ");
                    } else {
                        selectedRegionPosition.remove(p);
                        plugin.regions.put(p, new Location[]{first, b.getLocation()});
                        chat(p, "2. pozíció kijelölve. ");
                    }
                }
            if (b.getType() == ENDER_CHEST && b.getRelative(0, 1, 0).getType() == ENDER_STONE)
                p.openInventory(StringToInventory(plugin.hoadata.getProperty("extension_enderchest." + p.getUniqueId(), "Kiegészítő végzetláda|27"), p));
        }
        //System.out.println("click: " + b.getType() + " with yaw " + p.getLocation().getYaw() + " and pitch " + p.getLocation().getPitch());
    }

    @EventHandler
    public void on(PlayerGameModeChangeEvent evt) {
        Player p = evt.getPlayer();
        String InventoryToString = InventoryToString(p.getInventory());
        System.out.println(InventoryToString);
        plugin.hoadata.setProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(p.getWorld()) + ".main", InventoryToString);
        plugin.hoadata.setProperty("sepinv." + p.getGameMode() + "." + getworldinvcategory(p.getWorld()) + ".ender", InventoryToString(p.getEnderChest()));
        p.getInventory().clear();
        Inventory main = StringToInventory(plugin.hoadata.getProperty("sepinv." + evt.getNewGameMode() + "." + getworldinvcategory(p.getWorld()) + ".main", "|36"), null);
        for (ItemStack itemStack : main)
            if (itemStack != null)
                p.getInventory().addItem(itemStack);
        p.getEnderChest().clear();
        Inventory ender = StringToInventory(plugin.hoadata.getProperty("sepinv." + evt.getNewGameMode() + "." + getworldinvcategory(p.getWorld()) + ".ender", "|36"), null);
        for (ItemStack itemStack : ender)
            if (itemStack != null)
                p.getEnderChest().addItem(itemStack);
    }

    @EventHandler
    public void on(PlayerChatTabCompleteEvent evt) {
        if (evt.getLastToken().equals(evt.getChatMessage()) && evt.getLastToken().startsWith("@") && !evt.getLastToken().startsWith("@@")) {
            for (Player player : Bukkit.getOnlinePlayers())
                evt.getTabCompletions().add("@" + player.getName());
        }
    }
}
