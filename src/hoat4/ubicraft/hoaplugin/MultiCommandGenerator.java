/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.ubicraft.hoaplugin;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.StringReader;
import java.util.Scanner;

/**
 *
 * @author attila
 */
public class MultiCommandGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner((StringReader) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.plainTextFlavor));
        StringBuilder result = new StringBuilder();
        int delayCounter = 0;
        for (String line; !((line = scanner.nextLine()).equals("# end"));) {
            if (line.startsWith("# delay ")) {
                delayCounter += Integer.decode(line.substring("# delay ".length()));
                continue;
            }
            if (delayCounter > 0)
                line = "hpcmdblk delay " + delayCounter + " " + line;
            result.append("hpcmdblk multi ").append(line.split(" ").length).append(" ").append(line).append(" ");
        }
        result.append("hpcmdblk nop");
        System.out.println(result);
    }

}
