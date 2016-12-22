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

import me.vilsol.factorioupdater.managers.ModpackManager;
import me.vilsol.factorioupdater.managers.ProtocolManager;
import me.vilsol.factorioupdater.ui.UpdaterUI;
import me.vilsol.factorioupdater.util.StreamHandler;

public class Main {
    
    public static void main(String[] args){
        if(args.length >= 2 && args[0].equals("--protocol")){
            ProtocolManager.send(args[1]);
            return;
        }

        new Thread(new ProtocolManager()).start();

        StreamHandler.init();
        ModpackManager.getInstance();

        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.version: " + System.getProperty("os.version"));

        UpdaterUI.launch(args);
    }

}
