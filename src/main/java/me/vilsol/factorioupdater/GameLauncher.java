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

import com.jaunt.NotFound;
import com.jaunt.ResponseException;
import com.jaunt.UserAgent;
import com.jaunt.component.Form;
import me.vilsol.factorioupdater.managers.ModManager;
import me.vilsol.factorioupdater.util.Extract;
import me.vilsol.factorioupdater.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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

    public static void launchFactorio(File appDir, String[] arguments, File modDirectory) {
        List<String> launchArguments = new LinkedList<>();
        launchArguments.addAll(Arrays.asList(arguments));
        launchArguments.addAll(Arrays.asList("--mod-directory", modDirectory.getPath()));

        String os = System.getProperty("os.name", "UNKNOWN").toUpperCase();
        File binDir = new File(appDir, "bin");
        File executable;
        if (os.contains("WIN")) {
            executable = new File(binDir, "factorio.exe");
        } else if (os.startsWith("MAC")) {
            appDir = new File(appDir, "Contents");
            if (!binDir.exists()) {
                binDir = new File(appDir, "MacOS");
            }
            executable = new File(binDir, "factorio");
        } else if (!os.equals("UNKNOWN")) {
            executable = new File(binDir, "factorio");
        } else {
            System.err.println("Unknown operating system -- cannot launch");
            return;
        }
        launchArguments.add(0, appDir.toPath().relativize(executable.toPath()).toString());
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(launchArguments.toArray(new String[0]), null, appDir);

        } catch (IOException e) {
            e.printStackTrace();
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

        String urlRoot = "https://www.factorio.com/get-download/" + factorioVersion + "/alpha/";
        String downloadUrl = urlRoot;
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
        File installFile = new File(mainInstallDirectory, "factorio-files." + ext);
        if (!installFile.exists()) {
            try {
                UserAgent userAgent = ModManager.getInstance().getAgent().copy();
                userAgent.settings.maxRedirects = 1;
                userAgent.visit("http://www.factorio.com/login");

                Form form = userAgent.doc.getForm(0);
                form.setTextField("username_or_email", username);
                form.setPassword("password", password);
                form.submit();

                userAgent.visit(downloadUrl);
                return false;
            } catch (ResponseException e) {
                try {
                    downloadUrl = e.getRequestUrlData();
                    String[] spl = downloadUrl.split("\\?");
                    if (spl.length == 2 && spl[0].endsWith(ext)) {
                        mainInstallDirectory.mkdirs();
                        Utils.download(downloadUrl, installFile, downloadObserver);
                    } else {
                        e.printStackTrace();
                        return false;
                    }
                } catch (IOException ex) {
                    e.printStackTrace();
                    return false;
                }
            } catch (NotFound e) {
                e.printStackTrace();
                return false;
            }
        }

        return Extract.extract(installFile, directory, ext);
    }

}
