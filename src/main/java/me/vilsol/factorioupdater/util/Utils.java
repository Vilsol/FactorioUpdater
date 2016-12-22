/*
 * FactorioUpdater - The best factorio mod manager
 * Copyright 2016 The FactorioUpdater Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.vilsol.factorioupdater.util;

import com.google.common.io.ByteStreams;
import com.jaunt.HttpResponse;
import com.jaunt.UserAgent;
import me.vilsol.factorioupdater.managers.ModManager;
import me.vilsol.factorioupdater.models.ServerModRequirement;
import me.vilsol.factorioupdater.models.Version;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class Utils {
    
    //<input\s+type="hidden"\s+name="(.+?)"\s+value="(.+?)">
    
    public static String fetchURL(String url) {
        try{
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

            String whole = "";
            String inputLine;
            while((inputLine = in.readLine()) != null){
                whole += inputLine;
            }

            in.close();

            return whole;
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    public static <T extends Comparable<T>> T getLowest(T first, T second){
        return first.compareTo(second) < 0 ? first : second;
    }

    public static <T extends Comparable<T>> T getHighest(T first, T second){
        return first.compareTo(second) > 0 ? first : second;
    }
    
    public static byte[] deflate(byte[] source) {
        if (source == null || source.length == 0) {
            return source;
        }
        
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length / 2);
    
        try(OutputStream deflater = new DeflaterOutputStream(outputStream)){
            ByteStreams.copy(sourceStream, deflater);
        }catch(IOException e){
            e.printStackTrace();
        }
    
        return outputStream.toByteArray();
    }
    
    public static byte[] inflate(byte[] source) {
        if (source == null || source.length == 0) {
            return source;
        }
    
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length / 2);
    
        try(InputStream inflater = new InflaterInputStream(sourceStream)){
            ByteStreams.copy(inflater, outputStream);
        }catch(IOException e){
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
    
    public static byte[] base64Encode(byte[] raw) {
        return Base64.getEncoder().encode(raw);
    }
    
    public static byte[] base64Decode(byte[] encoded) {
        return Base64.getDecoder().decode(encoded);
    }
    
    public static boolean download(String url, File file) throws Exception {
        HttpResponse httpResponse = ModManager.getInstance().getAgent().sendHEAD(url);
        UserAgent agent = ModManager.getInstance().getAgent().copy();

        try {
            agent.download(httpResponse.getHeader("Location"), file);
        }catch (Exception e){
            file.delete();
            throw new RuntimeException(e);
        }

        return true;
    }

    public static void download(String urlString, File installFile, BiConsumer<Long, Long> downloadObserver) throws IOException {
        if (downloadObserver == null) {
            downloadObserver = (a, b) -> {
                double percent = a * 100.0 / b;
                String s = "\r[";
                int c = 50;
                for (int i = 0; i < c; i++) {
                    if (i * 100.0 / c <= percent)
                        s += (i + 1) * 100.0 / c <= percent ? "=" : ">";
                    else
                        s += " ";
                }
                s += String.format("] %.1f%% %s/%s", percent, toHumanReadable(a), toHumanReadable(b));
                System.out.print(s);
                System.out.flush();
                if (a == b)
                    System.out.println();
            };
        }
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        long contentLength = Long.valueOf(urlConnection.getHeaderField("Content-Length"));
        long progress = 0;
        try (InputStream inputStream = urlConnection.getInputStream()) {
            try (FileOutputStream outputStream = new FileOutputStream(installFile)) {
                int c = 1;
                byte[] buf = new byte[8192];
                while (c > 0) {
                    c = inputStream.read(buf, 0, buf.length);
                    if (c <= 0)
                        break;
                    outputStream.write(buf, 0, c);
                    progress += c;
                    downloadObserver.accept(progress, contentLength);
                }
            }
        }
    }

    public static String toHumanReadable(long l) {
        int depth = 0;
        while ((l & ~1023) > 0) {
            l >>= 10;
            depth++;
        }
        if (depth > 5)
            return "Unknown Size";
        return l + new String[]{ "B", "kB", "MB", "GB", "TB", "PB" }[depth];
    }
    
    public static Tuple<Map<String, List<String>>, String> sendPost(String url, Map<String, String> data, Map<String, String> headers) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        
        con.setRequestMethod("POST");
        
        if(headers != null){
            for(Map.Entry<String, String> e : headers.entrySet()){
                con.setRequestProperty(e.getKey(), e.getValue());
            }
        }
        
        if(data != null){
            String urlParameters = "";
    
            for(Map.Entry<String, String> e : data.entrySet()){
                if(!urlParameters.equals("")){
                    urlParameters += "&";
                }
        
                urlParameters += e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
            }
    
            System.out.println(urlParameters);
    
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
        }
    
        InputStream stream = con.getResponseCode() < 400 ? con.getInputStream() : con.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        
        in.close();
     
        return new Tuple<>(con.getHeaderFields(), response.toString());
    }
    
    public static Tuple<Map<String, List<String>>, String> sendGet(String url, Map<String, String> headers) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        
        con.setRequestMethod("GET");
    
        if(headers != null){
            for(Map.Entry<String, String> e : headers.entrySet()){
                con.setRequestProperty(e.getKey(), e.getValue());
            }
        }
    
        InputStream stream = con.getResponseCode() < 400 ? con.getInputStream() : con.getErrorStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder response = new StringBuilder();
    
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
    
        in.close();
    
        return new Tuple<>(con.getHeaderFields(), response.toString());
    }
    
    public static void login(String username, String password) throws Exception {
        System.out.println("STARTING LOGIN");
        String redirect = sendGet("https://mods.factorio.com/login", null).getFirst().get("Location").get(0);
        Tuple<Map<String, List<String>>, String> loginPage = sendGet(redirect, null);
        
        if(loginPage.getFirst().get("Location").size() > 0){
            loginPage = sendGet(loginPage.getFirst().get("Location").get(0), null);
        }
        
        System.out.println(loginPage);
        System.out.println("END LOGIN");
    }
    
    public static String readStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String inputLine;
        StringBuilder response = new StringBuilder();
    
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
    
        in.close();
    
        return response.toString();
    }
    
    public static List<ServerModRequirement> getServerMods(String server, int port) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(server);
        byte[] receiveData = new byte[16];
        DatagramPacket sendPacket = new DatagramPacket(new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, 12, IPAddress, port);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        
        byte[] data = receivePacket.getData();
        byte[] joinPacket = {0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, data[12], data[13], data[14], data[15], 0x60, (byte) 0xd1, (byte) 0x9a, (byte) 0xe0, 0x01, 0x56, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x04, 0x62, 0x61, 0x73, 0x65, 0x00, 0x0e, 0x15, 0x11, (byte) 0xee, (byte) 0xe2, 0x2e};
        int bufferSize = 1024;
        sendPacket = new DatagramPacket(joinPacket, joinPacket.length, IPAddress, port);
        clientSocket.send(sendPacket);
        receivePacket = new DatagramPacket(new byte[bufferSize], bufferSize);
        
        StringBuilder packets = new StringBuilder();
        boolean first = true;
        while(true){
            clientSocket.receive(receivePacket);
            byte[] d = cleanup(receivePacket.getData());
            String pack = new String(d, 0, d.length, StandardCharsets.US_ASCII);
            packets.append(first ? pack : pack.substring(4));
            
            if(pack.contains(String.valueOf(port))){
                break;
            }
            
            receivePacket.setData(new byte[bufferSize]);
            first = false;
        }
        
        String s = stripStart(packets.toString());
        int modCount = s.charAt(0);
        String modData = s.substring(4);
        
        List<ServerModRequirement> releases = new ArrayList<>();
        int offset = 0;
        for(int i = 0; i < modCount; i++){
            int len = modData.charAt(offset);
            String name = modData.substring(offset + 1, offset + 1 + len);
            int major = modData.charAt(offset + 1 + len);
            int minor = modData.charAt(offset + 1 + len + 1);
            int patch = modData.charAt(offset + 1 + len + 2);
            releases.add(new ServerModRequirement(name, new Version(major, minor, patch)));
            offset += len + 8;
        }
        
        clientSocket.close();
        return releases;
    }
    
    private static String stripStart(String data){
        int count = 0;
        char[] chars = data.toCharArray();
        String past = "";
        
        for(int i = 0; i < chars.length; i++){
            if((int) chars[i] == 0){
                count++;
                
                if(count == 4){
                    if(past.contains("server")){
                        return data.substring(i + 4);
                    }
                }
            }else{
                count = 0;
            }
            
            past += chars[i];
        }
        
        return data;
    }
    
    private static byte[] cleanup(byte[] data){
        int ex = data.length;
        
        for(int i = data.length - 1; i >= 0; i--){
            if(data[i] != 0){
                break;
            }
            
            ex--;
        }
        
        return Arrays.copyOfRange(data, 0, ex);
    }
    
}
