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

import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import me.vilsol.factorioupdater.util.HTMLUtil;
import me.vilsol.factorioupdater.util.StreamHandler;

/**
 * @author Nick Robson
 */
public class ConsoleUI {

    public static Tab createConsoleTab(UpdaterUI.Transitioner transition) {
        final WebView webView = new WebView();
        final WebEngine webEngine = webView.getEngine();
        final StringBuilder content = new StringBuilder();

        for (StreamHandler.WrappedString line : StreamHandler.lastLines)
            content.append(HTMLUtil.toHtml(line));
        webEngine.loadContent("<html><body><pre>" + content.toString() + "</pre></body></html>");

        Consumer<StreamHandler.WrappedString> consumer = line -> {
            content.append(HTMLUtil.toHtml(line));
            final String s = content.toString();
            Platform.runLater(() -> webEngine.loadContent("<html><body><pre>" + s + "</pre></body></html>"));
        };

        StreamHandler.getOutputStream().onLine(consumer);
        StreamHandler.getErrorStream().onLine(consumer);

        Tab tab = new Tab("Console", webView);
        tab.setClosable(false);
        System.out.println("Loaded console");
        return tab;
    }

}
