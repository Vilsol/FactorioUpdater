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

import java.io.File;

/**
 * @author Nick Robson
 */
public class Resource {

    public static final int SOCKET_PORT = 48256;

    public static final double FILE_SIZE_TO_MEGABYTES = 1049000d;

    public static final String APP_NAME = "Factorio Updater";

    public static final String URL_FACTORIO_MODS = "https://mods.factorio.com";
    public static final String URL_FACTORIO_MODS_MOD = "https://mods.factorio.com/api/mods/";

    public static final File APP_HOME_DIR;
    public static final String APP_HOME_NAME = "FactorioUpdater";

    public static final File APP_MODPACK_DIR;

    static{
        String os = System.getProperty("os.name").toUpperCase();
        File homeDir = new File(System.getProperty("user.home"));

        if(os.contains("WIN")){
            homeDir = new File(homeDir, "AppData" + File.separator + "Roaming" + File.separator + APP_HOME_NAME);
        }else{
            File macApplicationSupport = new File(homeDir, "Library/Application Support");
            if(os.contains("MAC") && macApplicationSupport.isDirectory()){
                homeDir = new File(macApplicationSupport, APP_HOME_NAME);
            }else{
                homeDir = new File(homeDir, "." + APP_HOME_NAME.toLowerCase());
            }
        }

        APP_HOME_DIR = homeDir;

        if(!APP_HOME_DIR.exists() && !APP_HOME_DIR.mkdirs()){
            throw new RuntimeException("Failed to create home directory. Check your permissions for: " + APP_HOME_DIR);
        }

        APP_MODPACK_DIR = new File(homeDir.getAbsolutePath(), "modpacks");

        if(!APP_MODPACK_DIR.exists() && !APP_MODPACK_DIR.mkdirs()){
            throw new RuntimeException("Failed to create modpacks directory. Check your permissions for: " + APP_MODPACK_DIR);
        }
    }

    public static void setUIProperties() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
        }
    }

}
