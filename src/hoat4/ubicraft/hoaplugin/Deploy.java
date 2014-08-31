/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hoat4.ubicraft.hoaplugin;

import java.io.IOException;

/**
 *
 * @author attila
 */
public class Deploy {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Initializing...");
        ProcessBuilder pb = new ProcessBuilder("scp", "dist/HoaBukkitPlugin.jar", 
                "mc:/opt/PrivGM0/plugins/HoaBukkitPlugin.jar");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        Process start = pb.start();
        System.out.println("Uploading...");
        start.waitFor();
        System.out.println("Done!");
    }
    
}