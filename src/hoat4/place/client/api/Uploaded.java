/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author hontvaria
 */
public class Uploaded {

    private String url;
    private String name;
    private long timestamp;

    private Uploaded(String url, String name) {
        this.url = url;
        this.name = name;
    }

    static Uploaded parse(String res) {
        Properties props = new Properties();
        try {
            props.load(new StringReader(res));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return new Uploaded(props.getProperty("upload.url"), props.getProperty("upload.name")).timestamp(Long.parseLong(props.getProperty("upload.timestamp")));
    }

    @Override
    public String toString() {
        return "Uploaded[name=" + name + ", url=" + url + ", timestamp=" + timestamp + ']';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(this.name, ((Uploaded) obj).name);
    }

    public static Uploaded byName(String name) {
        return new Uploaded(URL_PREFIX + name, name);
    }
    private static final String URL_PREFIX = "http://attila.hontvari.net:80/place/";

    public static Uploaded byURL(String url) {
        if (!url.startsWith(URL_PREFIX))
            throw new IllegalArgumentException("Not a place URL: " + url);
        return new Uploaded(url, url.substring(URL_PREFIX.length()));
    }

    /**
     * Sets this object's timestamp.
     *
     * @param timestamp the new value for the property called 'timestamp'
     * @return this Uploaded object
     */
    private Uploaded timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Gets this object's timestamp.
     *
     * @return timestamp the value of the property called 'timestamp'
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * Sets this object's name.
     *
     * @param name the new value for the property called 'name'
     * @return this Uploaded object
     */
    private Uploaded name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets this object's name.
     *
     * @return name the value of the property called 'name'
     */
    public String name() {
        return name;
    }
    private byte[] contentCache;

    public byte[] content(int options) throws IOException {
        if (contentCache == null || (options | DownloadOptions.FORCE_REDOWNLOAD) > 0)
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                int len = connection.getContentLength();
                InputStream in = connection.getInputStream();
                ByteArrayOutputStream out = len == -1 ? new ByteArrayOutputStream() : new ByteArrayOutputStream(len);
                byte[] buf = new byte[1024];
                for (int readed; (readed = in.read(buf)) > 0;)
                    out.write(buf, 0, readed);
                contentCache = out.toByteArray();
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        return contentCache;
    }

    public byte[] content() throws IOException {
        return content(0);
    }
    private Charset charset = Charset.defaultCharset();

    public Uploaded charset(String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    public String stringContent(int options) throws IOException {
        return new String(content(options), charset);
    }

    public String stringContent() throws IOException {
        return stringContent(0);
    }

    public String url() {
        return url;
    }
}
