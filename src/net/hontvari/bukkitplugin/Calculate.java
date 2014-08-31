/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.hontvari.bukkitplugin;

import java.util.Date;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.bukkit.Bukkit;
import static net.hontvari.bukkitplugin.HoaBukkitPlugin.chat;
/**
 *
 * @author attila
 */
public class Calculate implements Runnable {

    public static boolean isValid(String msg) {
        if (msg.startsWith("integer "))
            msg = msg.substring("integer ".length());
        try {
            return new ExpressionBuilder(msg).functions(functions).build().validate().isValid();
        } catch (Exception ignored) {
            return false;
        }
    }
    private String expression;
    private static final Function[] functions = new Function[]{
        new Function("time") {

            @Override
            public double apply(double... args) {
                return args[0] < 0.5 ? System.currentTimeMillis() : System.nanoTime();
            }
        }};
    private final HoaBukkitPlugin plugin;

    public Calculate(String expression, HoaBukkitPlugin plugin) {
        this.expression = expression;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        boolean asLong = false;
        if (expression.startsWith("integer ")) {
            expression = expression.substring("integer ".length());
            asLong = true;
        }
        double result = new ExpressionBuilder(expression).functions(functions).build().evaluate();
        if (asLong)
            chat('6', "calculator.resultPrefix", String.valueOf((long) result));
        else
            chat('6', "calculator.resultPrefix", String.valueOf(result));
    }
}
