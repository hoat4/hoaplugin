/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author attila
 */
public class RemoteLogger {

    private List<LogEntry> log = new CopyOnWriteArrayList<>();
    private final String appName;
    private final String appPart;

    public RemoteLogger(String appName, String appPart) {
        this.appName = appName;
        this.appPart = appPart;
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void crashed(Throwable ex, boolean fatal) {
        log.add(print(new Crashed(ex, fatal)));
        if (fatal)
            try {
                upload();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
    }

    public void info(String message) {
        log.add(print(new Normal(message)));
    }

    private void upload() throws IOException {
        UploadMeta meta = new UploadMeta();
        meta.author(appName + "::RemoteLogger");
        meta.securityLevel(PlaceSecurity.RS_3);
        meta.title(appName + "::" + appPart);

        StringBuilder sb = new StringBuilder();
        for (LogEntry logEntry : log)
            sb.append(logEntry).append('\n');
        meta.upload(sb.toString());
    }

    private LogEntry print(LogEntry entry) {
        if (entry instanceof Crashed)
            System.err.println(entry);
        else
            System.out.println(entry);
        return entry;
    }

    public RemoteLogger systemInfo() {
        log.add(print(new SystemInfo()));
        return this;
    }

    private static abstract class LogEntry {

        private long when;

        LogEntry() {
            when = ManagementFactory.getRuntimeMXBean().getUptime();
        }

        abstract String toRaw();

        @Override
        public String toString() {
            return "[" + when / 1000 + "." + when % 1000 + "] " + toRaw();
        }

    }

    private static class Crashed extends LogEntry {

        private final Throwable ex;
        private final boolean fatal;

        public Crashed(Throwable ex, boolean fatal) {
            this.ex = ex;
            this.fatal = fatal;
        }

        @Override
        public String toRaw() {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            return "Crash (" + (fatal ? "fatal" : "not fatal") + "): " + sw.toString();
        }

    }

    private static class Normal extends LogEntry {

        private final String className;
        private final String desc;

        public Normal(String desc) {
            String className1 = new Throwable().getStackTrace()[2].getClassName();
            this.className = className1.contains(".") ? className1.substring(className1.lastIndexOf(".") + 1) : className1;
            this.desc = desc;
        }

        @Override
        public String toRaw() {
            return className + ": " + desc;
        }

    }

    private static class SystemInfo extends LogEntry {

        @Override
        String toRaw() {
            return System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version");
        }
    }
}
