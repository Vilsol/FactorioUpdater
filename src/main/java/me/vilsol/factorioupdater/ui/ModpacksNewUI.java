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

import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.ModPack;
import me.vilsol.factorioupdater.util.WebUtils;
import org.json.JSONObject;

import java.io.File;
import java.util.Collections;

public class ModpacksNewUI {

    public static Tab createModPacksTab(UpdaterUI.Transitioner transition) {
        final WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();
    
        WebUtils.CustomContext context = new WebUtils.CustomContext();
        JSONObject json = new JSONObject("{\"schema\":\"v1\",\"mods\":{\"foo_bar\":{\"version\":\"1.0.0\",\"enabled\":true}},\"name\":\"Test Pack\",\"version\":\"1.2.3\",\"factorio_version\":\"0.14.20\"}");
        ModPack test_pack = ModPack.fromJSON(json, new File(Resource.APP_MODPACK_DIR, "test_pack"));
        context.setVariable("modpacks", Collections.singletonList(test_pack.map()));
        context.setVariable("css", WebUtils.getCSS("style.less"));
        String result = WebUtils.getTemplate("modpacks", context);
        webEngine.loadContent(result);
        
        Tab tab = new Tab("Web Modpacks", webView);
        tab.setClosable(false);
        return tab;
    }

}
