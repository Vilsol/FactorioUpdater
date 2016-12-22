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
package me.vilsol.factorioupdater.ui;

import javafx.scene.Parent;
import javafx.scene.control.TabPane;

/**
 * @author Nick Robson
 */
public class MainMenuUI {

    public static Parent create(UpdaterUI.Transitioner transition) {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(ModpacksUI.createModPacksTab(transition));
        tabs.getTabs().add(ModsUI.createModsTab(transition));
        tabs.getTabs().add(ConsoleUI.createConsoleTab(transition));
        tabs.getTabs().add(ModpacksNewFXUI.createModPacksTab(transition));
        tabs.getTabs().add(ModpacksNewFXResourceUI.createModPacksTab(transition));
        return tabs;
    }

}
