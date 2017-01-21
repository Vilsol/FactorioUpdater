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
package me.vilsol.factorioupdater;

import java.io.UncheckedIOException;
import me.vilsol.factorioupdater.managers.APIManager;
import me.vilsol.factorioupdater.models.DownloadTask;
import me.vilsol.factorioupdater.ui.templates.DownloadProgressWindow;
import me.vilsol.factorioupdater.util.DownloadPool;
import me.vilsol.factorioupdater.util.Extract;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author Nick Robson
 */
public class GameLauncher {
    
    /*
    
    /.factorioupdater
        - config.json // Contains all app-wide config and login credentials
        - credentials.json // Contains login information
        - /instances
            - {instance} // Each instance folder
                - pack.json // Contains instance specific information and modpack data
                - /factorio // The actual game folder
                    - /data
                    - /mods
                    - etc.


     */

    public static Process launchFactorioClient(File appDir, String[] arguments, File modDirectory) {
        List<String> launchArguments = new LinkedList<>();
        launchArguments.addAll(Arrays.asList(arguments));
        if (modDirectory != null && !launchArguments.contains("--mod-directory")) {
            launchArguments.addAll(Arrays.asList("--mod-directory", modDirectory.getPath()));
        }

        String os = System.getProperty("os.name", "UNKNOWN").toUpperCase();
        File binDir = new File(appDir, "bin");
        File executable;
        if (os.contains("WIN")) {
            executable = new File(binDir, "factorio.exe");
        } else if (os.startsWith("MAC")) {
            appDir = new File(appDir, "factorio.app");
            appDir = new File(appDir, "Contents");
            if (!binDir.exists()) {
                binDir = new File(appDir, "MacOS");
            }
            executable = new File(binDir, "factorio");
        } else if (!os.equals("UNKNOWN")) {
            executable = new File(binDir, "factorio");
        } else {
            throw new UnsupportedOperationException("Unknown operating system -- cannot launch");
        }
        launchArguments.add(0, appDir.toPath().relativize(executable.toPath()).toString());
        try {
            Runtime runtime = Runtime.getRuntime();
            return runtime.exec(launchArguments.toArray(new String[0]), null, appDir);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Downloads the Factorio files to a given directory.
     *
     * [directory]/bin/
     * [directory]/data/
     * (etc.)
     *
     * @param username
     * @param password
     * @param directory
     */
    public static boolean fetchFactorio(String factorioVersion, String username, String password, File directory, BiConsumer<Long, Long> downloadObserver) {
        String os = System.getProperty("os.name", "UNKNOWN").toUpperCase();
        String arch = System.getProperty("os.arch");

        String downloadUrl = "https://www.factorio.com/get-download/" + factorioVersion + "/alpha/";
        String ext;

        if (os.contains("WIN")) {
            downloadUrl += "win" + (arch.equals("x86") ? "32" : "64") + "-manual";
            ext = "zip";
        } else if (os.startsWith("MAC")) {
            downloadUrl += "osx";
            ext = "dmg";
        } else if (!os.equals("UNKNOWN")) {
            downloadUrl += "linux" + (arch.contains("64") ? "64" : "32");
            ext = "tar.gz";
        } else {
            System.err.println("Unknown operating system -- cannot download");
            return false;
        }
        
        File mainInstallDirectory = new File(Resource.APP_HOME_DIR, "versions");
        mainInstallDirectory = new File(mainInstallDirectory, factorioVersion);
        
        if(!mainInstallDirectory.exists()){
            if(!mainInstallDirectory.mkdirs()){
                throw new RuntimeException("Could not create directory: " + mainInstallDirectory);
            }
        }
        
        File installFile = new File(mainInstallDirectory, "factorio-files." + ext);
        if (!installFile.exists()) {
            try {
                APIManager.getInstance().login(username, password);
                HttpClient agent = APIManager.getInstance().getAgent();
                HttpResponse response = agent.execute(new HttpGet(downloadUrl));
                String location = response.getFirstHeader("Location").getValue();
                Long size = Long.valueOf(agent.execute(new HttpHead(location)).getFirstHeader("Content-Length").getValue());
                System.out.println(size);
    
                DownloadTask task = new DownloadTask("Factorio v" + factorioVersion, location, installFile, size, null);
    
                AtomicInteger skipper = new AtomicInteger();
                DownloadPool downloadPool = new DownloadPool(Collections.singletonList(task), progress -> {
                    if(skipper.getAndIncrement() % 20 != 0){
                        return;
                    }
                    
                    System.out.println(String.format("%.2f%%", (double) progress.getTotalDlSize() / (double) progress.getTotalSize() * 100));
                }, null);
                
                try{
                    DownloadProgressWindow progressWindow = new DownloadProgressWindow();
                    progressWindow.setDownloadPool(downloadPool);
                }catch(ExceptionInInitializerError ignored){
                }

                downloadPool.run();
                return true;
            } catch (Exception e) {
                throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
            }
        }

        return Extract.extract(installFile, directory, ext);
    }

}
