/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import hoat4.place.client.api.PlaceSecurity;
import hoat4.place.client.api.UploadMeta;
import hoat4.place.client.api.Uploaded;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.PacketPlayOutChat;
import static org.apache.commons.lang.StringUtils.join;
import static org.apache.commons.lang.StringUtils.reverse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 *
 * @author Attila
 */
public class HoaBukkitPlugin extends JavaPlugin {

    public static Properties logins = new Properties();

    public Properties hoadata = new Properties();
    public static HoapluginWebserver webserver;
    private static final File warpfile = new File("hoawarp");
    public static HoaBukkitPlugin instance;
    public final Set<String> ccp = new HashSet<>();
    public static Set<String> needLogin = new HashSet<>();
    public ServeStats serverStats;
    public Timer timer = new Timer("[HoaPlugin] Timer");
    boolean chatExperiment = true;
    long enabled;
    public PlotSys plotsys;
    public UbiCraft uc;
    public List<Integer> scheduled = new ArrayList<>();
    public boolean isEnabled;
    public Map<Player, Location[]> regions = new HashMap<>();
    public Set<UUID> mode36 = new HashSet<>();
    Set<String> lampbuilders = new HashSet<>();
    Set<String> betuzo = new HashSet<>();
    public static Properties i18n;
    public static Map<String, IDDescriptor> ids = new HashMap();

    @Override
    public void onEnable() {
        try {
            instance = this;
            chatExperiment = true;
            this.plotsys = new PlotSys(hoadata);
            stopWebServer();
            enabled = System.currentTimeMillis();
            isEnabled = true;
            logins = new Properties();
            i18n = new Properties();
            i18n.load(new ByteArrayInputStream(Uploaded.byName("05j9").charset("iso-8859-2").content()));//cy9v
            if (!new File("hoalogin").exists())
                storeLogins();
            else {
                loadLogins();
                String needloginStr = logins.getProperty("needLogin");
                if (needloginStr != null)
                    for (String pname : needloginStr.split(","))
                        if (Bukkit.getPlayer(pname) != null)
                            needLogin.add(pname);
            }
            String[] lines = Uploaded.byName("51zi").stringContent().split("\n");
            for (int i = 0; i < lines.length; i += 3)
                ids.put(lines[i].trim(), new IDDescriptor(lines[i].trim(), lines[i + 1], lines[i + 2]));
            this.uc = new UbiCraft(this);
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "Test");
            scheduled.add(Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L));
            /*  Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

             @Override
             public void run() {
             for (Player player : Bukkit.getOnlinePlayers()) {
             for (ItemStack itemStack : player.getInventory()) {
             if(itemStack.getItemMeta().getDisplayName().equals("Gránát"))
             player.sendMessage("found");
             }
             }
             }
             }, 1000, 50);*/
            Socket graphiteConn = new Socket("graphite3.in.hontvari.net", 2003);
            final BufferedWriter graphiteOut = new BufferedWriter(new OutputStreamWriter(graphiteConn.getOutputStream()));
            scheduled.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new GraphiteWriter(this, graphiteOut), 20, 40));
            scheduled.add(Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

                @Override
                public void run() {
                    if (HOAPLUGIN_JARFILE.lastModified() > enabled) {
                        reloadHoaPlugin(Bukkit.getConsoleSender());
                        enabled = System.currentTimeMillis();
                    }
                    Date d = new Date();
                    long time = Math.round(24000 * ((double) (d.getHours() * 3600000 + d.getMinutes() * 60000 + d.getSeconds() * 1000 + d.getTime() % 1000) / 0x5265C00)) - 6000;

                    for (World world : Bukkit.getWorlds())
                        world.setTime(time);
                    System.out.println(time);
                }
            }, 20, 10));

            //  timer.schedule(countMobs, 0, 2000);
            serverStats = new ServeStats(this);
            serverStats.onEnable();
            initWebApi();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Betölthetetlen warpfájl", ex);
        }
        getServer().getPluginManager().registerEvents(new BukkitListener(getServer(), this), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(uc), this);
        setEnabled(true);
    }

    public static String getServerName() {
        return "PrivGM0";
    }

    @Override
    public void onDisable() {
        logins.setProperty("needLogin", join(needLogin, ","));
        try {
            storeLogins();
        } catch (IOException ex) {
            handle(ex);
        }
        if (serverStats != null)
            serverStats.onDisable();
        timer.cancel();
        stopWebServer();
        Bukkit.getScheduler().cancelTasks(this);
        isEnabled = false;
    }
    public static volatile int idGenerator = 10000000;
    private static final Player nullPlayer = null;

    @Override
    @SuppressWarnings({"null", "CallToPrintStackTrace"})
    public boolean onCommand(CommandSender sender, Command cmd, String label, final String[] args) {
        long perfMeasureStartNSTime__ = System.nanoTime();
        try {
            final Player p = sender instanceof Player ? (Player) sender : nullPlayer;
            final Location l = p == null ? (sender instanceof ConsoleCommandSender ? null : ((BlockCommandSender) sender).getBlock().getLocation()) : p.getLocation();
            switch (cmd.getName()) {
                case "hoa":
                    if (p != null && l != null) {
                        if (args.length == 0) {
                            chat(p, "$5Elfelejtettél beírni valamit. ");
                            return true;
                        }
                        switch (args[0]) {
                            case "pjoin":
                                chat(createJoinMassage(p, false));
                                return true;
                            case "pquit":
                                chat(createQuitMassage(p));
                                return true;
                            case "ver":
                                chat(p, "[HoaPlugin] version=" + super.getDescription().getVersion());
                                return true;
                            case "wait":
                                try {
                                    Thread.sleep(Integer.decode(args[1]));
                                } catch (InterruptedException ex) {
                                    chat(p, "Waiting interrupted");
                                }
                                return true;
                            case "getpos":
                                Block b = p.getWorld().getBlockAt(l);
                                chat(p, "[HoaPlugin] Pozíció: x=" + l.getBlockX() + ",y=" + l.getBlockY() + ",z=" + l.getBlockZ());
                                chat(p, "[HoaPlugin] Blokk: " + b.getType() + "(" + b.getTypeId() + ")");
                                chat(p, "[HoaPlugin] Világ: " + p.getWorld());

                                return true;
                            case "addcc":
                                if (ccp.contains(args[1])) {
                                    chat(p, "[HoaPlugin] Már hozzá van adva " + args[1] + " a ccp listához");
                                    return true;
                                }
                                ccp.add(args[1]);
                                chat(p, "[HoaPlugin] Hozzáadva " + args[1] + " a ccp listához");
                                return true;
                            case "rmcc":
                                if (!ccp.contains(args[1])) {
                                    chat(p, "[HoaPlugin] " + args[1] + " nem volt hozzáadva a ccp listához");
                                    return true;
                                }
                                ccp.remove(args[1]);
                                chat(p, "[HoaPlugin] Kitörölve " + args[1] + " a ccp listából");
                                return true;
                            case "alvtp":
                                float xf = Float.parseFloat(args[2]);
                                float yf = Float.parseFloat(args[3]);
                                float zf = Float.parseFloat(args[4]);
                                p.teleport(new Location(Bukkit.getWorld(args[1]), xf, yf, zf));
                                return true;
                            case "rmmuted":
                                if (BukkitListener.muted.contains(args[1])) {
                                    BukkitListener.muted.remove(args[1]);
                                    chat(p, sender.getName() + " már nincs mutolva");
                                } else
                                    chat(p, sender.getName() + " nem volt mutolva");

                                return true;

                            case "serverstat_on":
                                serverStats.add((Player) sender);
                                return true;
                            case "serverstat_off":
                                serverStats.remove((Player) sender);
                                return true;
                            case "crash":
                                crash(Bukkit.getPlayer(args[1]));
                                return true;
                            case "chatnorm":
                                chatExperiment = !chatExperiment;
                                return true;
                            case "reversenames":
                                for (Player p1 : Bukkit.getOnlinePlayers()) {
                                    p1.setPlayerListName(reverse(p1.getPlayerListName()));
                                    chat(p, reverse(p1.getPlayerListName()) + " új neve: " + p1.getPlayerListName());
                                }
                                reverseNames = !reverseNames;
                                break;
                            case "reload":
                                reloadHoaPlugin(sender);
                                break;
                            case "jumpgen":
//                        new JumpGen(p).generate();
                                break;
                            case "warpbook":
                                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                                    private boolean bool;

                                    @Override
                                    public void run() {
                                        bool = !bool;
                                        final ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
                                        BookMeta bm = (BookMeta) item.getItemMeta();
                                        bm.setAuthor("HoaPlugin");
                                        bm.setTitle("title");
                                        bm.setPages(bool ? "1" : "2");
                                        item.setItemMeta(bm);
                                        p.getInventory().clear();
                                        final Location prev = l.clone();
                                        p.teleport(new Location(Bukkit.getWorld("world_nether"), 0, 5000, 0));
                                        Bukkit.getScheduler().runTaskLater(HoaBukkitPlugin.this, new Runnable() {

                                            @Override
                                            public void run() {
                                                p.teleport(prev);
                                                p.getInventory().addItem(item);
                                            }
                                        }, 40);
                                    }
                                }, 0, 80);
                                break;
                            case "killnear":
                                Location ploc = l;
                                double num = Double.parseDouble(args[1]);
                                AtomicInteger counter = new AtomicInteger();
                                p.getWorld().getEntities().stream().
                                        filter((entity) -> entity.getType() != EntityType.PLAYER).
                                        filter((entity) -> entity.getLocation().distance(ploc) < num).
                                        forEach((entity) -> {
                                            entity.remove();
                                            counter.incrementAndGet();
                                        });
                                chat(p, counter + " entitás törölve");
                                System.out.println(p + " removed " + counter + " entity");
                                break;
                            case "zrstart":
                                uc.startZR();
                                break;
                            case "zrstop":
                                uc.stopZR();
                                break;
                            case "plotsys_load":
                                p.teleport(new Location(plotsys.load(), 0, 100, 0));
                                break;
                            case "spawn_and_ride":
                                final Entity entity = p.getWorld().spawnEntity(l, EntityType.valueOf(args[1].toUpperCase()));
                                entity.setPassenger(p);
                                p.setMetadata("hoaride.ridingentity", new FixedMetadataValue(this, entity));
                                break;
                            case "mastart":
                                uc.startMA();
                                break;
                            case "mastop":
                                uc.stopMA();
                                break;
                            case "setwarp":
                                hoadata.setProperty("warp.pos." + args[1], posToTxt(l));
                                hoadata.setProperty("warp.desc." + args[1], Strings.join(Arrays.copyOfRange(args, 2, args.length), " "));
                                chat(p, "§aDáán. ");
                                break;
                            case "loadworld":
                                p.teleport(new Location(new WorldCreator(args[1]).createWorld(), 0, 100, 0));
                                break;
                            case "getwarpdef":
                                try {
                                    StringBuilder sb = new StringBuilder();
                                    if (args.length == 1)
                                        sb.append("tp @p ").append(l.getBlockX()).append(".5 ").append(l.getBlockY()).append(" ").append(l.getBlockZ()).append(".5");
                                    else
                                        sb.append("    public static final Location ").append(args[1]).append(" = pos(").append(l.getWorld().getName().equals(UbiCraft.varosWorldName) ? "varosWorldName" : ("\"" + l.getWorld().getName() + "\"")).append(", ").append(l.getBlockX()).append(".5, ").append(l.getBlockY()).append(", ").append(l.getBlockZ()).append(".5, ").append(Math.round(l.getYaw() / 45) * 45).append(args.length == 1 ? "" : ");");
                                    String url = warpdefMeta.author("HoaPlugin/" + p.getName()).
                                            charset("ISO 8859-2").securityLevel(PlaceSecurity.RS_3).upload(sb.toString()).url();
                                    chatJSON(p, "{text:\"WarpDef from "+p.getDisplayName()+"\",clickEvent:{action:open_url,value:\"" + url + "\"}}");
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                                break;
                            case "getregiondef":
                                Location first = regions.get(p)[0];
                                Location second = regions.get(p)[1];
                                int x1 = Math.min(first.getBlockX(), second.getBlockX());
                                int z1 = Math.min(first.getBlockZ(), second.getBlockZ());
                                int x2 = Math.max(first.getBlockX(), second.getBlockX());
                                int z2 = Math.max(first.getBlockZ(), second.getBlockZ());
                                String content = "if (l.getBlockX() >= " + x1 + " && l.getBlockZ() >= " + z1 + " && l.getBlockX() <= " + x2 + " && l.getBlockZ() <= " + z2 + ")";
                                try {
                                    chat(p, UploadMeta.create().title("UC::RegionDef").author("HoaPlugin/" + p.getName()).upload(content).url());
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                                break;
                            case "creditcard":
                                ItemStack result = new ItemStack(90);
                                ItemMeta meta = result.getItemMeta();
                                meta.setDisplayName("§fBankkártya");
                                //meta.addEnchant(Enchantment.DURABILITY, 1000, true);
                                meta.setLore(Arrays.asList("Tulajdonos: §d" + p.getName()));
                                result.setItemMeta(meta);
                                p.getInventory().addItem(result);
                                break;
                            case "cartest":
                                uc.setupCar();
                                break;
                            case "36":
                                if (mode36.contains(p.getUniqueId())) {
                                    mode36.remove(p.getUniqueId());
                                    chat(p, "§c36-as mód kikacsolva. ");
                                } else {
                                    mode36.add(p.getUniqueId());
                                    chat(p, "§236-as mód bekapcsolva. ");
                                }
                                break;
                            case "lampbuilder":
                                if (lampbuilders.contains(p.getName()))
                                    lampbuilders.remove(p.getName());
                                else
                                    lampbuilders.add(p.getName());
                                break;
                            case "setbiome":
                                first = regions.get(p)[0];
                                second = regions.get(p)[1];
                                Biome biome = Biome.valueOf(args[1]);
                                for (int x = Math.min(first.getBlockX(), second.getBlockX()); x < Math.max(first.getBlockX(), second.getBlockX()); x++)
                                    for (int z = Math.min(first.getBlockZ(), second.getBlockZ()); z < Math.max(first.getBlockZ(), second.getBlockZ()); z++) {
                                        p.getWorld().setBiome(x, z, biome);
                                        chat(p, x + ", " + z);
                                    }
                                chat(p, "Kész!");
                                break;
                            case "vote":
                                String yes = "{text:\" [Igen]\",clickEvent:{action:\"run_command\",value:\"@" + p.getName() + " yes\"}}";
                                String no = "{text:\"[Nem]\",clickEvent:{action:\"run_command\",value:\"@" + p.getName() + " no\"}}";
                                broadcast("{text:\"" + escapeJSON(String.join(" ", Arrays.copyOfRange(args, 1, args.length))) + "\",extra:[" + yes + ',' + no + "]}");
                                break;
                            case "invsee":
                                p.closeInventory();
                                if (Bukkit.getPlayer(args[1]) == null)
                                    chat(p, "§c" + String.format(i18n("cmd.invsee.playerNotFound"), args[1]));
                                else if (args.length > 2)
                                    p.openInventory(Bukkit.getPlayer(args[1]).getEnderChest());
                                else
                                    p.openInventory(Bukkit.getPlayer(args[1]).getInventory());
                                break;
                            case "rank":
                                switch (args[1]) {
                                    case "get":
                                        chat(p, args[2] + ": " + getrank(Bukkit.getPlayer(args[2])));
                                        break;
                                    case "set":
                                        setrank(Bukkit.getPlayer(args[2]), Rank.valueOf(args[3]));
                                        chat(p, "§2Done. ");
                                        break;
                                }
                                break;
                            case "money":
                                switch (args[1]) {
                                    case "get":
                                        chat(p, args[2] + ": " + getmoney(Bukkit.getPlayer(args[2])));
                                        break;
                                    case "set":
                                        setmoney(Bukkit.getPlayer(args[2]), Integer.decode(args[3]));
                                        chat(p, "§2Done. ");
                                        break;
                                }
                                break;
                            /*  case "clearproperty":  // SECURITY RISK!
                             hoadata.remove(args[1]);
                             break;*/
                            case "betuzes":
                                if (betuzo.contains(p.getName()))
                                    betuzo.remove(p.getName());
                                else
                                    betuzo.add(p.getName());
                                break;
                        }
                    } else {
                        chat(p, "Nem ember vagy. ");
                        return false;
                    }
                    return true;
                case "selfmsg":
                    chat(p, String.join(" ", args));
                    return true;
                case "hoa_gc":
                    //   Bukkit.broadcastMessage("[HoaPlugin] GC végrehajtása...");
                    Runtime r = Runtime.getRuntime();
                    long freebef = r.freeMemory();
                    System.gc();
                    long freeaft = r.freeMemory();
                    long res = (freeaft - freebef) / 1000000;
                    chat("[HoaPlugin] GC: " + res + " MB RAM felszabadítva");
                    return true;
                case "plainmsg":
                    Bukkit.getPlayer(args[0]).sendMessage(Arrays.copyOfRange(args, 1, args.length));
                    break;
                case "register":
                    if (args.length != 1) {
                        chat(p, "Használat: ");
                        return false;
                    }
                    if (logins.containsKey(sender.getName()))
                        chat(p, i18n("cmd.register.alreadyRegistered"));
                    else {
                        logins.setProperty(sender.getName(), args[0]);
                        needLogin.remove(sender.getName());
                        chat(p, i18n("cmd.register.success"));
                    }
                    return true;
                case "login":
                    if (args.length != 1) {
                        chat(p, "Használat: ");
                        return false;
                    }
                    if (!needLogin.contains(sender.getName())) {
                        chat(p, i18n("cmd.login.alreadyLoggedIn"));
                        return true;
                    }
                    if (!logins.containsKey(sender.getName()))
                        chat(p, i18n("cmd.login.registerFirst"));
                    else if (logins.get(sender.getName()).equals(args[0])) {
                        needLogin.remove(sender.getName());
                        chat(p, i18n("cmd.login.done"));
                    } else
                        chat(p, i18n("cmd.login.badPassword"));
                    return true;
                case "unregister":
                    logins.remove(sender.getName());
                    p.kickPlayer(i18n("cmd.unregister.done"));
                    return true;
                case "sinfo":
                    if (args.length < 1) {
                        chat(p, i18n("cmd.sinfo.usage.noparam"));
                        return false;
                    }
                    switch (args[0]) {
                        case "on":
                            serverStats.add((Player) sender);
                            break;
                        case "off":
                            serverStats.remove((Player) sender);
                            break;
                        default:
                            chat(p, i18n("cmd.sinfo.usage.unknownmode"));
                            return false;
                    }
                    return true;
                case "sendtoconsolehidden":
                    return true;
                case "afk":
                    if (args.length > 0) {
                        String[] argscopy = args;
                        if (args[args.length - 1].endsWith("ni"))
                            args[args.length - 1] = args[args.length - 1].substring(0, args[args.length - 1].indexOf("ni"));
                        if (args.length > 1 && args[0].equals("elment"))
                            argscopy = Arrays.copyOfRange(args, 1, args.length);

                        String msg = Strings.join(argscopy, " ");
                        if (argscopy[argscopy.length - 1].equals("en"))
                            afks.put(p, p.getDisplayName() + " visszajött innen: " + Strings.join(Arrays.copyOf(argscopy, argscopy.length - 1), " ") + " evés. ");
                        else if (argscopy.length == 1 && argscopy[0].equals("meghal"))
                            afks.put(p, String.format(i18n("cmd.afk.againLives"), p.getDisplayName()));
                        else
                            afks.put(p, p.getDisplayName() + " visszajött innen: " + msg + (argscopy[argscopy.length - 1].equals("néz") ? "és" : "ás"));
                        System.out.println(argscopy.length + argscopy[0] + ";");
                        chat(sender.getName() + " elment " + msg + "ni. ");
                    } else {
                        afks.put(p, p.getDisplayName() + " újra itt van, és Minecraftozik. ");
                        chat(String.format(i18n("cmd.afk.noDetail"), p.getDisplayName()));
                    }
                    return true;
                case "fly":
                    p.setAllowFlight(!p.getAllowFlight());
                    if (p.getAllowFlight())
                        chat(p, i18n("cmd.fly.turnedOn"));
                    else
                        chat(p, i18n("cmd.fly.turnedOff"));
                    return true;
                case "ci":
                    if (hasrank(p, Rank.VIP)) {
                        p.getInventory().clear();
                        chat(p, i18n("cmd.ci.done"));
                    } else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "warp":
                    if (args.length == 1)
                        try {
                            p.teleport((Location) Warps.class.getDeclaredField(args[0]).get(null));
                            return true;
                        } catch (NoSuchFieldException ex) {
                            chat(p, "§c" + String.format(i18n("cmd.warp.warpNotFound"), "§4" + args[0]));
                        } catch (IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                            throw new RuntimeException(ex);
                        }
                    StringBuilder jsonBuilder = new StringBuilder("{text:\"" + i18n("cmd.warp.warps") + ": \",extra:[");
                    for (Field field : Warps.class.getDeclaredFields()) {
                        if (!field.isAnnotationPresent(Warp.class))
                            continue;
                        Warp warp = field.getAnnotation(Warp.class);
                        jsonBuilder.append("{text:\"/").append(warp.before()).append(field.getName()).append("\",hoverEvent:{action:show_text,value:\"").append(warp.desc()).append("\"},clickEvent:{action:run_command,value:\"/warp ").append(field.getName()).append("\"}},");
                        jsonBuilder.append("{text:\"").append(warp.after()).append("\"},");
                    }
                    jsonBuilder.append("{text:\"\"}]}");
                    System.out.println(jsonBuilder);
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(jsonBuilder.toString()), true));
                    return true;
                case "sudo":
                    if (args[1].equals("afk")) {
                        chat(p, i18n("cmd.sudo.blocked.afk"));
                        return true;
                    }
                    PluginCommand pluginCommand = Bukkit.getPluginCommand(args[1]);
                    if (pluginCommand == null) {
                        chat(p, String.format(i18n("cmd.sudo.noSuchCommand"), args[1]));
                        return true;
                    }
                    Player player = Bukkit.getPlayer(args[0]);
                    if (player == null) {
                        chat(p, String.format(i18n("cmd.sudo.noSuchPlayer"), args[1]));
                        return true;
                    }
                    pluginCommand.execute(player, args[1], Arrays.copyOfRange(args, 2, args.length));
                    if (p != null)
                        chat(p, String.format(i18n("cmd.sudo.done"), args[1]));
                    return true;
                case "i":
                    Material material;
                    try {
                        material = Material.getMaterial(Integer.decode(args[0]));
                    } catch (NumberFormatException ex) {
                        material = Material.valueOf(args[0].toUpperCase());
                    }
                    p.getInventory().addItem(new ItemStack(material));
                    return true;
                case "gmc":
                    if (p.isOp() || (hasrank(p, Rank.KING) && !p.getWorld().getName().equals("SkyPvP")))
                        p.setGameMode(GameMode.CREATIVE);
                    else
                        chat(p, "§4" + i18n("cmd.heal.noPermission"));
                    return true;
                case "gms":
                    p.setGameMode(GameMode.SURVIVAL);
                    return true;
                case "heal":
                    if (p.isOp()) {
                        p.setHealth(20.0);
                        p.setFoodLevel(20);
                        chat(p, "§2Done. ");
                    } else
                        chat(p, "§4" + i18n("cmd.heal.noPermission"));
                    return true;
                case "home":
                    p.teleport(toPos(hoadata.getProperty("home." + p.getName())));
                    return true;
                case "sethome":
                    hoadata.setProperty("home." + p.getName(), posToTxt(l));
                    return true;
                case "speed":
                    if (hasrank(p, Rank.VIP))
                        try {
                            p.setFlySpeed(Float.parseFloat(args[0]));
                        } catch (NumberFormatException ex) {
                            chat(p, "§c" + String.format(i18n("cmd.speed.nan"), args[0]));
                        }
                    else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "hu_maradjtalpon_start":
                    World world = sender instanceof BlockCommandSender ? ((BlockCommandSender) sender).getBlock().getWorld() : l.getWorld();
                    long perfMeasureStartNSTime__b = System.nanoTime();
                    maradjtalponCounter++;
                    for (int x = 20075; x <= 20085; x++)
                        for (int z = 19919; z <= 19929; z++) {
                            final Block b = world.getBlockAt(x, 60, z);
                            final int cntfinal = maradjtalponCounter;
                            Bukkit.getScheduler().runTaskLater(this, new Runnable() {

                                @Override
                                public void run() {
                                    if (cntfinal == maradjtalponCounter)
                                        b.setType(Material.AIR);
                                }
                            }, (long) (Math.random() * 2000));
                            if (b.getType() != Material.MELON_BLOCK)
                                b.setType(Material.MELON_BLOCK);
                        }
                    chat(p, "Done @ " + (System.nanoTime() - perfMeasureStartNSTime__b) / 1000 + " µs");
                    return true;
                case "hpcmdblk":
                    System.out.println("HpCmdBlk");
                    switch (args[0]) {
                        case "newissue":
                            Date d = new Date();
                            Hopper hopper = (Hopper) Bukkit.getWorld(UbiCraft.varosWorldName).getBlockAt(-27, 46, 14).getState();
                            int count = 0;
                            for (ItemStack itemStack : hopper.getInventory())
                                if (itemStack != null && itemStack.getType() == Material.WRITTEN_BOOK) {
                                    BookMeta book = (BookMeta) itemStack.getItemMeta();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(book.getAuthor()).append(": ").append(book.getTitle()).append("\n\n");
                                    book.getPages().forEach((page)
                                            -> sb.append(page).append('\n'));
                                    sb.append("\nUbiCraft spawn, ").append(d.getYear() + 1900).append(". ").append(d.getMonth() + 1).append(". ").append(d.getDate()).append(" ").append(d.getHours()).append(":").append(d.getMinutes()).append(":").append(d.getSeconds()).append("\n");
                                    try {
                                        UploadMeta.create().author("HoaPlugin/" + book.getAuthor()).title("UC::Issue").upload(sb.toString());
                                    } catch (IOException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    count++;
                                    System.out.println("book!");
                                }
                            hopper.getInventory().clear();
                            String string = count + " panasz érkezett egyik játékostól. ";
                            System.out.println("[HPCMDBLK] New issue(s): " + count + "!");
                            if (count > 0)
                                multibroadcast(string, "hoat_pra", "zsohajdu1", "Mine20", "Andris907", "ADRIENN200312");
                            break;
                        case "spleefrestore":
                            world = sender instanceof BlockCommandSender ? ((BlockCommandSender) sender).getBlock().getWorld() : l.getWorld();
                            perfMeasureStartNSTime__b = System.nanoTime();
                            for (int x = 20054; x < 20094; x++)
                                for (int z = 19960; z < 19991; z++) {
                                    Block b = world.getBlockAt(x, 66, z);
                                    if (b.getType() != Material.SNOW_BLOCK)
                                        b.setType(Material.SNOW_BLOCK);
                                }
                            chat(p, "Done @ " + (System.nanoTime() - perfMeasureStartNSTime__b) / 1000 + " µs");
                            break;
                        case "btoft":
                            Bukkit.getPlayer(args[1]).teleport(Warps.fatelepPortal);
                            break;
                        case "jaillift":
                            int target = 50 + 4 * Integer.decode(args[1]);
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                System.out.println(onlinePlayer);
                                Location loc = onlinePlayer.getLocation();
                                if (loc.getBlockX() >= 54 && loc.getBlockZ() >= 32 && loc.getBlockX() <= 56 && loc.getBlockZ() <= 34) {
                                    chat(onlinePlayer, "Liftezés...");
                                    Location loc2 = loc.clone();
                                    loc2.setY(target);
                                    onlinePlayer.teleport(loc2);
                                }
                            }
                            break;
                        case "setupvelocity":
                            Vector orig = Bukkit.getPlayer(args[1]).getVelocity();
                            Bukkit.getPlayer(args[1]).setVelocity(new Vector(orig.getX() + (args.length > 3 ? Double.parseDouble(args[3]) : 0), Double.parseDouble(args[2]), orig.getZ() + (args.length > 4 ? Double.parseDouble(args[4]) : 0)));
                            break;
                        case "multi":
                            int firstlen = Integer.decode(args[1]);
                            String first = String.join(" ", Arrays.copyOfRange(args, 2, firstlen + 2));
                            String second = String.join(" ", Arrays.copyOfRange(args, 2 + firstlen, args.length));

                            Bukkit.dispatchCommand(sender, first);
                            Bukkit.dispatchCommand(sender, second);
                            break;
                        case "sethealth":
                            Bukkit.getPlayerExact(args[1]).setHealth(Double.parseDouble(args[2]));
                            break;
                        case "extrahealth":
                            Bukkit.getPlayerExact(args[1]).setMaxHealth(Double.parseDouble(args[3]));
                            Bukkit.getPlayerExact(args[1]).setHealth(Double.parseDouble(args[2]));
                            break;
                        case "ifgamemode":
                            if (Bukkit.getPlayerExact(args[1]).getGameMode().toString().equals(args[2]))
                                Bukkit.dispatchCommand(sender, String.join(" ", Arrays.copyOfRange(args, 4, 4 + Integer.decode(args[3]))));
                            else
                                Bukkit.dispatchCommand(sender, String.join(" ", Arrays.copyOfRange(args, 4 + Integer.decode(args[3]), args.length)));
                            break;
                        case "delay":
                            Bukkit.getScheduler().runTaskLater(this, () -> {
                                Bukkit.dispatchCommand(sender, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                            }, Integer.decode(args[1]));
                            break;
                    }
                    return true;
                case "plotme":
                case "telek":
                case "p":
                case "t":
                    switch (args[0]) {
                        case "claim":
                        case "c":
                        case "vesz":
                        case "v":
                            plotsys.claim(p);
                            break;
                        case "home":
                        case "haza":
                        case "honvágy":
                        case "h":
                            plotsys.goHome(p);
                            break;
                        case "version":
                        case "verzió":
                            plotsys.aboutPlotSys(p);
                            break;
                        case "testgui":
                            chat(p, "[Hoa_UCCmd] ucgui");
                            break;
                    }
                    return true;
                case "tp":
                    if (args.length > 3 && p == null) {
                        Player man = Bukkit.getPlayer(args[0]);
                        man.teleport(new Location(man.getWorld(), Double.parseDouble(args[1]), Double.parseDouble(args[2]), Double.parseDouble(args[3])));
                    } else if (args.length == 3 && p.isOp())
                        p.teleport(new Location(p.getWorld(), Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2])));
                    else if (Bukkit.getPlayer(args[0]) == null)
                        chat(p, "§c" + String.format("cmd.tp.noSuchPlayer", args[0]));
                    else
                        p.teleport(Bukkit.getPlayer(args[0]));
                    return true;
                case "give":
                    if (Bukkit.getPlayer(args[0]) == null)
                        chat(p, "§c" + String.format("cmd.tp.noSuchPlayer", args[0]));
                    else if (hasrank(Bukkit.getPlayer(args[0]), Rank.MODERATOR))
                        Bukkit.getPlayer(args[0]).getInventory().addItem(new ItemStack(material(args[1]), args.length > 2 ? Integer.decode(args[2]) : 1, args.length > 3 ? Short.decode(args[3]) : 0));
                    else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "workbench":
                    if (args.length > 0) {
                        Inventory inv = Bukkit.createInventory(p, InventoryType.WORKBENCH);
                        int i = 0;
                        for (String string : args[0].split(","))
                            inv.setItem(i++, new ItemStack(material(string)));
                        p.openInventory(inv);
                        return true;
                    }
                    if (hasrank(p, Rank.COOL))
                        p.openWorkbench(null, true);
                    else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "enderchest":
                    if (hasrank(p, Rank.COOL))
                        p.openInventory(p.getEnderChest());
                    else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "edit":
                    replaceChat(Integer.decode(args[0]), (uuid)
                            -> args[0] + "::" + createChatMessage(Integer.decode(args[0]), uuid.equals(p.getUniqueId()), p, String.join(" ", Arrays.copyOfRange(args, 1, args.length)))
                    );
                    renderChat(p);
                    return true;
                case "say":
                    System.out.println("cmdselecttime: " + (System.nanoTime() - perfMeasureStartNSTime__) / 1000 + " µs");
                    long perfMeasureStartNSTime__a = System.nanoTime();

                    if (hasrank(p, Rank.COOL))
                        chat("§4[" + sender.getName() + "]§f " + String.join(" ", args));
                    else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    System.out.println("Time: " + (System.nanoTime() - perfMeasureStartNSTime__a) / 1000 + " µs");
                    return true;
                case "bearaz":
                    if (hasrank(p, Rank.VIP)) {
                        if (p.getItemInHand() != null) {
                            ItemMeta meta = p.getItemInHand().getItemMeta();
                            meta.setDisplayName(ChatColor.COLOR_CHAR + "6Ár: " + Integer.decode(args[0]) + " UbiCoin / db");
                            p.getItemInHand().setItemMeta(meta);
                        }
                    } else
                        chat(p, i18n("cmd.gmc.noPermission"));
                    return true;
                case "nick":
                    p.setDisplayName(args[0]);
                    break;
                case "kill":
                    if (hasrank(p, Rank.ADMIN))
                        if (args.length > 0)
                            Bukkit.getPlayer(args[0]).setHealth(0);
                        else
                            p.setHealth(0);
                    else
                        chat(p, "cmd.gmc.noPermission");
                    break;
            }
            try {
                p.teleport((Location) Warps.class.getDeclaredField(cmd.getName()).get(null));
                return true;
            } catch (NoSuchFieldException ex) {
                //chat(p, "§cNincs ilyen warp: §4" + args[0]);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                handle(ex);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            handle((Player) sender, ex);
        }
        return false;
    }
    private final UploadMeta warpdefMeta = UploadMeta.create().title("UC::WarpDef");
    private int maradjtalponCounter;
    boolean reverseNames;
    public Map<Player, String> afks = new HashMap<>();

    private String pos(Player p) {
        Location loc = p.getLocation();
        return loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    public static String createJoinMassage(Player p, boolean originalMC) {
        return String.format(i18n("automatic.joinMessage"), p.getDisplayName());
    }

    public static String createQuitMassage(Player p) {
        return String.format(i18n("automatic.quitMessage"), p.getDisplayName());
    }

    private void pos(Player p, String s) {
        String[] locs = s.split(",");
        p.getLocation().setX(Double.parseDouble(locs[0]));
        p.getLocation().setY(Double.parseDouble(locs[1]));
        p.getLocation().setZ(Double.parseDouble(locs[2]));
    }

    private void initWebApi() throws IOException {
        if (webserver == null) {
            System.out.println("[HoaPlugin] Starting web API...");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        webserver = new HoapluginWebserver();
                        webserver.start();
                        System.out.println("[HoaPlugin] Started web API. ");
                    } catch (IOException ex) {
                        Logger.getLogger(HoaBukkitPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).start();
        }
    }

    private void stopWebServer() {
        if (webserver != null) {
            System.out.println("[HoaPlugin] Stopping web API...");
            webserver.stop();
            webserver = null;
        }
    }

    public String processRegistration(Map<String, String> parms) {
        String pname = parms.get("hoa_autoreg_name");
        String ppasswd = parms.get("hoa_autoreg_passwd");
        String ad = parms.get("hoa_autoreg_ad");
        if (logins.contains(pname))
            return "fail:already_registered";
        logins.setProperty(pname, ppasswd);
        return "ok:norm";
    }

    private void storeLogins() throws FileNotFoundException, IOException {
        try (FileWriter writer = new FileWriter("hoalogin")) {
            logins.store(writer, "HoaPlugin login file");
        }
        try (FileWriter writer = new FileWriter("hoa.dat")) {
            hoadata.store(writer, "HoaPlugin data file");
        }
    }

    private void loadLogins() throws FileNotFoundException, IOException {
        logins = new Properties();
        try (FileReader reader = new FileReader("hoalogin")) {
            logins.load(reader);
        }
        try (FileReader reader = new FileReader("hoa.dat")) {
            hoadata.load(reader);
        }
    }

    void onLeave(Player p) {
        if (ServeStats.noscore.contains(p))
            ServeStats.noscore.remove(p);
    }

    public static void crash(Player p) {
        /* PacketPlayOutBlockChange packet53 = new PacketPlayOutBlockChange();
         packet53.block = net.minecraft.server.v1_7_R1.Block.p.getLocation().getBlock().getX();
         packet53.data = 200;
         ServeStats.sendPacket(p, packet53);*/
    }

    private void reloadHoaPlugin(CommandSender sender) {
        chat('2', "automatic.update.started");
        Bukkit.reload();
        chat('a', "automatic.update.done");
    }

    public static final File HOAPLUGIN_JARFILE = new File("/opt/" + getServerName() + "/plugins/HoaBukkitPlugin.jar");

    private Location toPos(String property) {
        if (property == null)
            return null;
        String[] split = property.split(Pattern.quote("|"));
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    private String posToTxt(Location loc) {
        return loc.getWorld().getName() + "|" + loc.getX() + "|" + loc.getY() + "|" + loc.getZ() + "|" + loc.getYaw() + "|" + loc.getPitch();
    }

    private void multibroadcast(String message, String... players) {
        for (String playerName : players) {
            Player p = Bukkit.getPlayerExact(playerName);
            if (p != null)
                chat(p, message);
        }
    }

    private void broadcast(String string) {
        for (Player p : Bukkit.getOnlinePlayers())
            chat(string);
    }

    public Rank getrank(Player p) {
        return Rank.valueOf(hoadata.getProperty(p.getUniqueId() + ".rank", "PLAYER"));
    }

    public void setrank(Player p, Rank rank) {
        hoadata.setProperty(p.getUniqueId() + ".rank", rank.name());
    }

    public boolean hasrank(Player p, Rank rank) {
        return getrank(p).ordinal() >= rank.ordinal();
    }

    Material material(String name) {
        if (name.startsWith("minecraft:"))
            name = name.substring("minecraft:".length());
        try {
            return Material.getMaterial(Integer.decode(name));
        } catch (NumberFormatException ex) {
            return Material.valueOf(name.toUpperCase());
        }
    }

    public static String i18n(String id) {
        return i18n.getProperty(id, id);
    }

    private void handle(java.lang.Exception ex) {
        throw new RuntimeException(ex);
    }

    public static String InventoryToString(Inventory invInventory) {
        String serialization = invInventory.getTitle() + "|" + invInventory.getSize() + ";";
        for (int i = 0; i < invInventory.getSize(); i++) {
            ItemStack is = invInventory.getItem(i);
            if (is != null) {
                String serializedItemStack = new String();

                String isType = String.valueOf(is.getType().getId());
                serializedItemStack += "t@" + isType;

                if (is.getDurability() != 0) {
                    String isDurability = String.valueOf(is.getDurability());
                    serializedItemStack += ":d@" + isDurability;
                }

                if (is.getAmount() != 1) {
                    String isAmount = String.valueOf(is.getAmount());
                    serializedItemStack += ":a@" + isAmount;
                }

                Map<Enchantment, Integer> isEnch = is.getEnchantments();
                if (isEnch.size() > 0)
                    for (Entry<Enchantment, Integer> ench : isEnch.entrySet())
                        serializedItemStack += ":e@" + ench.getKey().getId() + "@" + ench.getValue();

                serialization += i + "#" + serializedItemStack + ";";
            }
        }
        return serialization;
    }

    @SuppressWarnings("null")
    public static Inventory StringToInventory(String invString, InventoryHolder holder) {
        String title = invString.substring(0, invString.indexOf('|'));
        invString = invString.substring(invString.indexOf('|') + 1);
        String[] serializedBlocks = invString.split(";");
        String invInfo = serializedBlocks[0];
        Inventory deserializedInventory = Bukkit.getServer().createInventory(holder, Integer.valueOf(invInfo), title);
        for (int i = 1; i < serializedBlocks.length; i++) {
            String[] serializedBlock = serializedBlocks[i].split("#");
            int stackPosition = Integer.valueOf(serializedBlock[0]);

            if (stackPosition >= deserializedInventory.getSize())
                continue;

            ItemStack is = null;
            Boolean createdItemStack = false;

            String[] serializedItemStack = serializedBlock[1].split(":");
            for (String itemInfo : serializedItemStack) {
                String[] itemAttribute = itemInfo.split("@");
                if (itemAttribute[0].equals("t")) {
                    is = new ItemStack(Material.getMaterial(Integer.valueOf(itemAttribute[1])));
                    createdItemStack = true;
                } else if (itemAttribute[0].equals("d") && createdItemStack)
                    is.setDurability(Short.valueOf(itemAttribute[1]));
                else if (itemAttribute[0].equals("a") && createdItemStack)
                    is.setAmount(Integer.valueOf(itemAttribute[1]));
                else if (itemAttribute[0].equals("e") && createdItemStack)
                    is.addUnsafeEnchantment(Enchantment.getById(Integer.valueOf(itemAttribute[1])), Integer.valueOf(itemAttribute[2]));
            }
            deserializedInventory.setItem(stackPosition, is);
        }

        return deserializedInventory;
    }

    public static String LocToString(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append(loc.getX()).append('_');
        sb.append(loc.getY()).append('_');
        sb.append(loc.getZ()).append("__");
        sb.append(loc.getWorld().getUID());
        return sb.toString();
    }

    public static Location StringToLoc(String input) {
        String[] split = input.split("__");
        String[] coords = split[0].split("_");
        String worldName = String.join("__", Arrays.copyOfRange(split, 1, split.length));
        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        double z = Double.parseDouble(coords[2]);
        World world = Bukkit.getWorld(UUID.fromString(worldName));
        return new Location(world, x, y, z);
    }
    private static final Map<UUID, List<String>> chatMap = new HashMap<>();
    private static final String e = "-1::{text:\"\"}";
    private static final Collection<String> manyEmptyLines = Arrays.asList(e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e, e);
    private static List<String> nextChatMapValue = new ArrayList<>(manyEmptyLines);
    public static AtomicInteger cid = new AtomicInteger();

    public static int nextChatID(Player player) {
        return chatMap.getOrDefault(player.getUniqueId(), Collections.EMPTY_LIST).size();
    }

    public static void chat(Player player, String message) {
        chatJSON(player, "{text:\"" + escapeJSON(message) + "\"}");
    }

    public static String escapeJSON(String message) {
        return message.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static void chatJSON(Player p, String message) {
        String msg = i18n(message);
        if (chatMap.putIfAbsent(p.getUniqueId(), nextChatMapValue) == null)
            nextChatMapValue = new ArrayList<>(manyEmptyLines);
        chatMap.get(p.getUniqueId()).add(cid.get() + "::" + msg);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(msg)));
        //renderChat(player);
    }

    public static void chat(Player player, String... message) {
        StringBuilder sb = new StringBuilder();
        for (String part : message)
            sb.append(' ').append(i18n(part));
        chat(player, sb.toString().substring(1));
    }

    public static void chat(Player player, char color, String message) {
        chat(player, "§" + color + i18n(message));
    }

    public static void chat(Player player, char color, String... message) {
        StringBuilder sb = new StringBuilder("§" + color);
        for (String part : message)
            sb.append(' ').append(i18n(part));
        chat(player, sb.toString().substring(1));
    }

    public static void chat(String player, String message) {
        chat(Bukkit.getPlayer(player), message);
    }

    public static void chat(String player, char color, String message) {
        chat(Bukkit.getPlayer(player), color, message);
    }

    public static void chat(String message, Player... players) {
        for (Player player : players)
            chat(player, message);
    }

    public static void chat(char color, String message, Player... players) {
        for (Player player : players)
            chat(player, color, message);
    }

    public static void chat(char color, Player[] players, String... message) {
        for (Player player : players)
            chat(player, color, message);
    }

    public static void chat(char color, String message) {
        chat(color, message, Bukkit.getOnlinePlayers());
    }

    public static void chat(char color, String... message) {
        chat(color, Bukkit.getOnlinePlayers(), message);
    }

    public static void chat(String message) {
        chat(String.join(" ", message), Bukkit.getOnlinePlayers());
    }

    private static void renderChat(Player player) {
        System.out.println("renderChat: " + player);
        for (String json : chatMap.get(player.getUniqueId())) {
            String substring = json.substring(json.indexOf("::") + 2);
            //   System.out.println(substring);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(substring)));
        }
    }
    private String chatFormat;

    {
        try {
            chatFormat = Uploaded.byName("l5gn").stringContent().replace("\r", "").replace("\n", "").replace("  ", "");
        } catch (IOException ex) {
            Logger.getLogger(HoaBukkitPlugin.class.getName()).log(Level.SEVERE, null, ex);
            chatFormat = ex.toString();
        }
    }

    private static final List<String> blacklist = Arrays.asList("anyád", "kurva", "bazd", "picsa", "picsába", "basz", "fasz", "pöcs", "szar", "fos", "szopd", "rohadj", "fuck", "buzi", "kúr", "csöves", "geci", "segg", "pina", "antinormális hülyegyerek", "shit", "homokos", "kapd be", "buzeráns", "k*rva", "ku*va", "kur*a", "f@sz", "k***a", "anyad", "fák ju", "kuss");
    private static final List<String> blacklist2 = Arrays.asList("istenem", "csak", "de istenem");
    private static final List<String> whitelist = Arrays.asList("winfos", "Ubuntu");
    private final Set<Player> wrongworded = new HashSet<>();

    private final Random random = new Random();

    public String createChatMessage(int chatID, boolean self, Player p, String message) {
        if (message.startsWith("6login ") || message.equals("login " + HoaBukkitPlugin.logins.getProperty(p.getName()))) {
            secCantSend(p);
            return null;
        }
        if (HoaBukkitPlugin.needLogin.contains(p.getName()))
            chat('4', String.format(i18n("censor.writesButNotLoggedIn"), p.getName()));
        if (BukkitListener.muted.contains(p.getName())) {
            chat(p, "§4" + i18n("censor.mute.cantWrite"));
            System.out.println("[HoaPlugin] " + p.getName() + " beszélni próbálkozott, de le volt mutolva káromkodás miatt.");
            return null;
        }
        if (message.equals("!do pex reload")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pex reload");
            chat(p, "PEX reloadolva");
            return null;
        }
        message = message.replace(":)", "☻").replace(":(", "☹").replace("<3", "❤").replace(":/", "\uD83D\uDE15");
        String lcmsg = message.toLowerCase().replace("@", "a");
        boolean containsWhitelisted = false;
        for (String string : whitelist)
            if (lcmsg.contains(string))
                containsWhitelisted = true;

        boolean badMsg = false;
        if (!containsWhitelisted)
            for (String blackword : blacklist)
                if (lcmsg.contains(blackword))
                    badMsg = true;
                else if (blackword.length() > 4)
                    for (int i = 0; i < blackword.length(); i++) {
                        String str = blackword.substring(0, i) + blackword.substring(i + 1);
                        if (lcmsg.contains(str))
                            badMsg = true;
                    }

        if (badMsg && !containsWhitelisted) {
            wrongworded.add(p);
            // muted.add(p.getName());
            //chat(p,  "["+p.getWorld().getName()+"]< "+p.getDisplayName()+"> "+message);
            System.out.println("[HoaPlugin] Karomkodott " + p.getName() + ": " + message);
            boolean classmate = p.getName().equals("zsohajdu1") || p.getName().equals("valamilyenember") || p.getName().equals("hoat_pra") || p.getName().equals("Andris907") || p.getName().equals("Monster24");
            int rnd = random.nextInt(6);
            if (classmate)
                if (rnd < 1)
                    chat(p, "§4Ez káromkodás.");
                else if (rnd < 2)
                    chat(p, "§cEz nem Csernák-kompatiblis üzenet. ");
                else if (rnd < 3)
                    chat(p, "§4Az ilyen szavakat inkább hagyjuk...");
                else if (rnd < 4 && !p.getName().equals("Monster24") && !p.getName().equals("valamilyenember") && !p.getName().equals("mondokz") && !p.getName().equals("Andris907"))
                    chat(p, "§4Már te is kezded?!");
                else if (rnd < 5)
                    chat(p, "§4Tessék?!");
                else
                    chat(p, "§4Ne káromkodj!");
            else if (rnd < 1)
                chat(p, "§4" + i18n("censor.swear.1"));
            else if (rnd < 2)
                chat(p, "§4" + i18n("censor.swear.2"));
            else if (rnd < 3 && wrongworded.contains(p))
                chat(p, "§4" + i18n("censor.swear.3"));
            else if (rnd < 4)
                chat(p, "§4" + i18n("censor.swear.4"));
            else if (rnd < 5)
                chat(p, "§4" + i18n("censor.swear.5"));
            else
                chat(p, "§4" + i18n("censor.swear.6"));

            return null;
        }
        if (blacklist2.contains(lcmsg) && !containsWhitelisted) {
            // muted.add(p.getName());
            //chat(p,  "["+p.getWorld().getName()+"]< "+p.getDisplayName()+"> "+message);
            System.out.println("[HoaPlugin] Tiltott üzenetet használt " + p.getName() + ": " + message);
            chat(p, "§4" + i18n("censor.blockedMessage"));
            return null;
        }
        if (message.toUpperCase().equals(message))
            message = lcmsg;
        if (lcmsg.contains("??")) {
            chat(p, "§4!" + i18n("censor.tooManyQuestionaries"));
            return null;
        }
        if (lcmsg.contains("!!")) {
            chat(p, "§4" + i18n("censor.tooManyExclamationMarks"));
            return null;
        }
        if (betuzo.contains(p.getName()))
            message = String.join(". ", message.split("")) + ". ";
        switch (lcmsg) {
            case "hoat":
            case "zsohajdu":
            case "admin":
            case "tulaj":
            case "ati":
            case "hoatpra":
            case "hoat_pra":
            case "zsohajdu1":
            //  case "Mine20":
            case "t ulaj":
            case "hor_pra":
            case "hot_pra":
            case "hoat-pra":
            case "hoat_pra.":
                chat(p, "§c" + i18n("censor.writeInPrivateToStaff"));
                return null;
        }
        if (Calculate.isValid(message))
            Bukkit.getScheduler().runTask(this, new Calculate(message, this));
        if (message.length() < 3)
            message = message.replace(":d", ":D");
        if (chatExperiment)
            message = changeChat(message);
        Date date = new Date();
        String value = chatFormat.
                replace("$H", String.valueOf(date.getHours())).
                replace("$M", String.valueOf(date.getMinutes())).
                replace("$S", String.valueOf(date.getSeconds())).
                replace("$W", uc.getWorldDisplayName(p.getWorld())).
                replace("$C", getrank(p).colorForJSON()).
                replace("$R", i18n("rank." + getrank(p).name())).
                replace("$c", "white").
                replace("$P", p.getDisplayName()).
                replace("$m", escapeJSON(message)).
                replace("$I", chatID + " " + message).
                replace("$B", self ? "E" : "").
                replace("$p", "X: " + p.getLocation().getBlockX() + " / Y: " + p.getLocation().getBlockY() + " / Z: " + p.getLocation().getBlockZ() + "\\nF: " + p.getLocation().getPitch());
        System.out.println(value.replace("$B", "E"));
        return value;
    }

    private String changeChat(String msg) {
        if (msg.isEmpty())
            return msg;
        msg = msg.trim();
        if (!msg.endsWith(".") && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(",") && !msg.endsWith(":)") && !msg.endsWith(":D"))
            msg += ".";
        msg = msg.substring(0, 1).toUpperCase() + msg.substring(1);
        return msg;
    }

    private void secCantSend(Player sender) {
        System.out.println("seccantsend");
        chat(sender, "censor.secCantSend");
    }

    private void replaceChat(int cid, Function<UUID, String> valueWithCID) {
        long perfMeasureStartNSTime__ = System.nanoTime();
        String prefix = cid + "::";
        List<UUID> toBeRerendered = new ArrayList<>();
        for (Map.Entry<UUID, List<String>> entry : chatMap.entrySet()) {
            List<String> list = entry.getValue();
            boolean found = false;
            for (int i = 0; i < list.size(); i++) {
                String string = list.get(i);
                if (string.startsWith(prefix)) {
                    list.set(i, valueWithCID.apply(entry.getKey()));
                    found = true;
                }
            }
            if (found)
                toBeRerendered.add(entry.getKey());
        }
        System.out.println("replaceChat part 1 perf: " + (System.nanoTime() - perfMeasureStartNSTime__) / 1000 + " µs");
        perfMeasureStartNSTime__ = System.nanoTime();
        for (Player player : Bukkit.getOnlinePlayers())
            if (toBeRerendered.contains(player.getUniqueId()))
                renderChat(player);

        System.out.println("replaceChat part 2 perf: " + (System.nanoTime() - perfMeasureStartNSTime__) / 1000 + " µs");
    }

    private void handle(Player player, Exception ex) {
        chat(player, ex.toString());
        printStackTrace(player, ex);
        Throwable cause = ex;
        while (cause.getCause() != null)
            cause = cause.getCause();

    }

    private void printStackTrace(Player player, Exception ex) {
        for (StackTraceElement elem : ex.getStackTrace()) {
            if (elem.getFileName().equals("PlayerConnection.java") && elem.getMethodName().equals("handleCommand") && elem.getLineNumber() == 984)
                break;
            String[] classNameSplit = elem.getClassName().split(Pattern.quote("."));
            for (int i = 0; i < classNameSplit.length; i++)
                if (i != classNameSplit.length - 1)
                    classNameSplit[i] = classNameSplit[i].substring(0, 1);
            String string = "    " + String.join(".", classNameSplit) + "." + elem.getMethodName()
                    + (elem.isNativeMethod() ? "(Native Method)"
                    : (elem.getFileName() != null && elem.getLineNumber() >= 0
                    ? " (" + (elem.getLineNumber()) + ')'
                    : (elem.getFileName() != null ? "(" + elem.getFileName() + ")" : "(Unknown Source)")));
            if (elem.getClassName().startsWith(HoaBukkitPlugin.class.getPackage().getName())) {
                String json = "{text:\"" + string + "\",clickEvent:{action:open_url,value:\"http://attila.hontvari.net:80/place/p4xv?" + (elem.getLineNumber() - 1) + "#" + elem.getFileName().substring(0, elem.getFileName().length() - 5) + "\"}}";
                System.out.println(json);
                chatJSON(player, json);
            } else
                chat(player, string);
        }
        if (ex.getCause() != null) {
            chat(player, "Caused by " + ex.getCause().toString());
            printStackTrace(player, (Exception) ex.getCause());
        }
    }

    public static class IDDescriptor {

        public String displayName;
        public String name;
        private final String id;

        private IDDescriptor(String id, String string, String string0) {
            displayName = string.substring(0, string.length() - 1);
            name = string0.substring(1, string0.length() - 2);
            this.id = id;
        }

        @Override
        public String toString() {
            return id + ": " + name + " = " + displayName;
        }

    }

    public int getmoney(Player p) {
        return Integer.decode(hoadata.getProperty(p.getUniqueId() + ".money", "0"));
    }

    public void setmoney(Player p, int money) {
        hoadata.setProperty(p.getUniqueId() + ".money", money + "");
    }
}
