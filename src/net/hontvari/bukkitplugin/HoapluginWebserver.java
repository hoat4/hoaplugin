/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import fi.iki.elonen.NanoHTTPD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 *
 * @author attila
 */
public class HoapluginWebserver extends NanoHTTPD {


    public HoapluginWebserver() {
        super(8080);
    }

    @Override
    public Response serve(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        try {
            switch (uri) {
                case "/recovery/debug":
                    return serverDebugPage(uri, method, header, parms, files);
                case "/":
                    return serverDefaultPage();
                case "registration.js":
                    return serverRegistratePage(parms);
                case "/recovery/":
                    return servePage("recovery",new String[][]{
                        new String[]{"infobar", ""},
                    });
                case "/recovery/do_recovery":
                    return serveRecoveryActionPage(parms);
            }
            return serve404();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder(ex.toString());
            for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
                sb.append(stackTraceElement).append('\n');
            }
            return new Response(sb.toString());
        }

    }

    private Response serverDebugPage(String uri, Method method, Map<String, String> header, Map<String, String> parms, Map<String, String> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head><title>Debug Page - ProfiCraft Recovery</title></head>");
        sb.append("<body>");
        sb.append("<h1>Response</h1>");
        sb.append("<p><blockquote><b>URI -</b> ").append(String.valueOf(uri)).append("<br />");
        sb.append("<b>Method -</b> ").append(String.valueOf(method)).append("</blockquote></p>");
        sb.append("<h3>Headers</h3><p><blockquote>").append(String.valueOf(header)).append("</blockquote></p>");
        sb.append("<h3>Parms</h3><p><blockquote>").append(String.valueOf(parms)).append("</blockquote></p>");
        sb.append("<h3>Files</h3><p><blockquote>").append(String.valueOf(files)).append("</blockquote></p>");
        sb.append("</body>");
        sb.append("</html>");
        return new Response(sb.toString());
    }

    private Response serverDefaultPage() {
        return new Response(Response.Status.OK, MIME_HTML, getClass().getResourceAsStream("default.html"));
    }

    private Response serve404() {
        return new Response("404 Not Found. Hibás URI. ");
    }

    private Response serverRegistratePage(Map<String, String> parms) {
        try {
            String result = HoaBukkitPlugin.instance.processRegistration(parms);
            return new Response(Response.Status.OK, "text/javascript", "mc_reg_ok('" + result + "');");
        } catch (Exception ex) {
            long time = System.currentTimeMillis();
            String id = "err_reg_" + Long.toHexString(time);
            System.out.println("[HoaPlugin] Hiba: " + id);
            System.err.println("[HoaPlugin] Hiba: " + id);
            ex.printStackTrace();
            return new Response(Response.Status.OK, "text/javascript", "mc_reg_err('" + id + "')");
        }
    }

    private Response servePage(String name, String[]... params) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(name+".html")));
        StringBuilder sb = new StringBuilder();
        for(int ch ;(ch = reader.read())!=-1;ch++) {
            sb.append((char)ch);
        }
        String str = sb.toString();
        for (String[] strings : params) {
           str =  str.replace("${"+strings[0]+"}", strings[1]);
        }
        return new Response(Response.Status.OK, MIME_HTML, str);

    }

    private Response serveRecoveryActionPage(final Map<String, String> parms) throws IOException {
        final String pname = parms.get("uname");
        final String action = parms.get("action");
      //  System.out.println("[Recovery] Killing ");
        Bukkit.getScheduler().runTask(HoaBukkitPlugin.instance, new Runnable() {

            @Override
            public void run() {
                try{
                OfflinePlayer oplayer = Bukkit.getOfflinePlayer(pname);
                Player player = Bukkit.getPlayer(pname);
                switch(action) {
                    case "Kill":
                player.setHealth(0.0);
                        break;
                    case "Kick":
                        player.kickPlayer("Kickelve lettél");
                        break;
                    case "Ban":
                        oplayer.setBanned(true);
                        player.kickPlayer("Kaptál egy bant");
                        break;
                    case "Unban":
                        oplayer.setBanned(false);
                        break;
                    case "Op":
                        oplayer.setOp(true);
                        break;
                    case "Deop":
                        oplayer.setOp(false);
                        break;
                    case "Crash":
                        HoaBukkitPlugin.crash(player);
                        break;
                    case "tellraw":
                        player.sendRawMessage(parms.get("val"));
                        break;
                    case "Reload":
                        System.out.println("[HoaPluginRecovery] Performing reload...");
                        Bukkit.reload();
                        break;
                }}catch(Throwable ex) {
                    ex.printStackTrace();
                }
            }
        });
        System.out.println("Sering do_recovery...");
        return servePage("recovery",new String[][]{
                        new String[]{"infobar", "block"},
                        new String[]{"infobar_text", action+" végrehajtva "+pname+" nevű játékosnak"}
                    });
    }

}
