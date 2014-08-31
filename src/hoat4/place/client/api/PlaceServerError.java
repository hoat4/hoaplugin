/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *
 * @author attila
 */
class PlaceServerError extends Error {

    /**
     * Constructs an instance of <code>PlaceError</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public PlaceServerError(String msg) {
        super(msg);
    }

    static PlaceServerError from(Scanner input) {
        final String nextLine = input.nextLine();
        if (nextLine.startsWith("place.api.error="))
            return new PlaceServerError(nextLine.substring("place.api.error=".length()));
        else
            if (nextLine.trim().startsWith("<html>")) {
                input.findWithinHorizon(Pattern.quote("<pre>\n"), 0);
                String errLine = input.nextLine();
                /*if (errLine.startsWith("java.lang.RuntimeException: "))
                    errLine = errLine.substring(28);*/
                return new PlaceServerError(errLine);
            } else
                return new PlaceServerError(nextLine);
    }
}
