/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author attila
 */
public enum PlaceSecurity {

    RS(0, 4), RS_2(2, 64), RS_3(1, 16);

    final int code;
    public final int length;
    private PlaceSecurity(int code, int length) {
        this.code = code;
        this.length = length;
    }
    // fore 33 ns, HashSet 7 ns, search-by-hashcode 14 ns
    private static final Set<String> RS_TXT = new HashSet<>(Arrays.asList("0x1", "a", "1", "rs", "rs1", "rs_0x1", "4", "r1", "s1"));
    private static final Set<String> RS2_TXT = new HashSet<>(Arrays.asList("0x40", "2", "r2", "s2", "rs2", "rs_2", "rs_0x2", "64"));
    private static final Set<String> RS3_TXT = new HashSet<>(Arrays.asList("0x10", "3", "r3", "s3", "rs3", "rs_3", "rs_0x3"));
    public static PlaceSecurity get(String input) {
        String inputLowercase = input.toLowerCase();
        if (RS_TXT.contains(inputLowercase))
            return RS;
        if (RS2_TXT.contains(inputLowercase))
            return RS_2;
        if (RS3_TXT.contains(inputLowercase))
            return RS_3;
        return null;
    }
}
