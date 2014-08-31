/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hoat4.place.client.api;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *
 * @author hontvaria
 */
public class UploadMeta {

    public Uploaded upload(byte[] content) throws IOException {
        ByteArrayOutputStream bios = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bios);
        encode(dos);
        //appendParam(query, "api", "true");
        dos.write(content);
        final String res = executePost("http://attila.hontvari.net/place/", bios.toByteArray());
        return Uploaded.parse(res);
    }

    private String executePost(String targetURL, byte[] urlParameters) throws IOException, PlaceServerError//src:http://www.xyzws.com/Javafaq/how-to-use-httpurlconnection-post-data-to-web-server/139
    {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "binary/octet-stream");

            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream out = connection.getOutputStream();
            out.write(urlParameters);
            out.flush();

            //Get Response	
            InputStream is;
            try {
                is = connection.getInputStream();
            } catch (IOException ex) {
                final Scanner scanner = new Scanner(connection.getErrorStream());
                throw PlaceServerError.from(scanner);
            }
            StringBuilder response;
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append("\r\n");
            }
            rd.close();
            return response.toString();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    public Uploaded upload(String content) throws IOException {
        try {
            return upload(content.getBytes(validCharset()));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Uploaded upload(Path file) throws IOException {
        return upload(Files.readAllBytes(file));
    }

    /**
     * Reads the specified {@link InputStream}, wraps it into an UploadMeta, and
     * closes the specified {@link InputStream}.
     *
     * @param input
     * @return the newly created {@link UploadMeta} object
     * @throws IOException if I/O error occurs
     */
    public Uploaded uploadAndClose(InputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int ch; (ch = input.read()) != -1;)
            baos.write(ch);
        input.close();
        return upload(baos.toByteArray());
    }

    private static final String DEFAULT_CHARSET = "UTF-8";
    private String charset = DEFAULT_CHARSET;

    /**
     * Sets this object's charset. If this parameter is null, the value of
     * property will be the default value. The default value of this property is
     * currently "UTF-8".
     *
     * @param charset the new value for the property called 'charset'
     * @return this UploadMeta object
     */
    public UploadMeta charset(String charset) {
        if (charset == null)
            this.charset = DEFAULT_CHARSET;
        else
            this.charset = charset;
        return this;
    }

    /**
     * Gets this object's charset.
     *
     * @return charset the value of the property called 'charset'
     */
    public String charset() {
        return charset;
    }

    private static final String DEFAULT_AUTHOR = "";
    private String author = DEFAULT_AUTHOR;

    /**
     * Sets this object's author. If this parameter is null, the value of
     * property will be the default value. The default value of this property is
     * currently "UTF-8".
     *
     * @param author the new value for the property called 'author'
     * @return this UploadMeta object
     */
    public UploadMeta author(String author) {
        if (author == null)
            this.author = DEFAULT_AUTHOR;
        else
            this.author = author;
        return this;
    }

    /**
     * Gets this object's author.
     *
     * @return author the value of the property called 'author'
     */
    public String author() {
        return author;
    }

    private static final int DEFAULT_SECURITY_LEVEL = PlaceSecurity.RS_3.code;
    private int securityLevel = DEFAULT_SECURITY_LEVEL;
    private static final PlaceSecurity[] sectypes = new PlaceSecurity[]{PlaceSecurity.RS, PlaceSecurity.RS_3, PlaceSecurity.RS_2};

    /**
     * Sets this object's securityLevel. If this parameter is null, the value of
     * property will be the default value. The default value of this property is
     * currently PlaceSecurity.RS.
     *
     * @param securityLevel the new value for the property called
     * 'securityLevel'
     * @return this UploadMeta object
     */
    public UploadMeta securityLevel(PlaceSecurity securityLevel) {
        if (securityLevel == null)
            this.securityLevel = PlaceSecurity.RS.code;
        else
            this.securityLevel = securityLevel.code;
        return this;
    }

    /**
     * Gets this object's securityLevel.
     *
     * @return securityLevel the value of the property called 'securityLevel'
     */
    public PlaceSecurity securityLevel() {
        return sectypes[securityLevel];
    }

    private static final String DEFAULT_MIME_TYPE = "text/plain";
    private String mimeType = DEFAULT_MIME_TYPE;

    /**
     * Sets this object's mimeType. If this parameter is null, the value of
     * property will be the default value. The default value of this property is
     * currently "text/plain".
     *
     * @param mimeType the new value for the property called 'mimeType'
     * @return this UploadMeta object
     */
    public UploadMeta mimeType(String mimeType) {
        if (mimeType == null)
            this.mimeType = DEFAULT_MIME_TYPE;
        else
            this.mimeType = mimeType;
        return this;
    }

    /**
     * Gets this object's mimeType.
     *
     * @return mimeType the value of the property called 'mimeType'
     */
    public String mimeType() {
        return mimeType;
    }
    private String title = "";

    /**
     * Sets this object's title.
     *
     * @param title the new value for the property called 'title'
     * @return this UploadMeta object
     */
    public UploadMeta title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets this object's title.
     *
     * @return title the value of the property called 'title'
     */
    public String title() {
        return title;
    }

    private void encode(DataOutputStream dos) throws IOException {
        dos.writeBytes("\n" + (filters.size() + 8) + "\n");
        appendParam(dos, "api", "true");
        appendParam(dos, "input_id", "3");
        appendParam(dos, "metaAuthor", author);
        appendParam(dos, "metaMimeType", mimeType);
        appendParam(dos, "metaCharset", charset);
        if (title != null)
            appendParam(dos, "metaLabel", title);
        appendParam(dos, "sectype", Integer.toString(securityLevel));
        appendParam(dos, "filter.count", Integer.toString(filters.size()));
        int i = 0;
        for (Map.Entry<String, UploadMeta> entry : filters.entrySet()) {
            appendParam(dos, "filter[" + i + "]", entry.getValue().encodeToString());
            i++;
        }
    }

    private void appendParam(DataOutputStream query, String name, String value) throws IOException {
        /*    query.writeBytes("--");
         query.writeBytes(key);
         query.writeBytes("\r\nContent-Disposition: form-data; name=\"");
         query.writeBytes(name);
         query.writeBytes("\"\r\n\r\n");
         query.writeBytes(value);
         query.writeBytes("\r\n");*/
        String val = URLEncoder.encode(value, isCharsetValid() ? charset : "UTF-8");
        String encoded = name + "=" + val + "\n";
        query.writeBytes(encoded);
    }

    private boolean isCharsetValid() {
        if (charset == null)
            return false;
        try {
            return Charset.forName(charset) != null;
        } catch (IllegalCharsetNameException ex) {
            return false;
        } catch (IllegalArgumentException ex) {//UnsupportedCharsetException extends IllegalArgumentException
            return false;
        }
    }

    public String validCharset() {
        return isCharsetValid() ? charset : "UTF-8";
    }

    public static UploadMeta create() {
        return new UploadMeta();
    }
    private final Map<String, UploadMeta> filters = new HashMap<>();

    public UploadMeta addFilter(String filterName, UploadMeta to) {
        filters.put(filterName, to);
        return this;
    }

    private String encodeToString() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        encode(new DataOutputStream(baos));
        return URLEncoder.encode(baos.toString(), validCharset());
    }
}
