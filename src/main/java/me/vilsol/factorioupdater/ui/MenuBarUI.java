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

import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import me.vilsol.factorioupdater.util.HTMLUtil;
import me.vilsol.factorioupdater.util.StreamHandler;

import java.util.function.Consumer;

/**
 * @author Nick Robson
 */
public class MenuBarUI {

    public static MenuBar create() {
        MenuBar menubar = new MenuBar();
        String os = System.getProperty("os.name", "unknown");
        if (os.startsWith("Mac"))
            menubar.setUseSystemMenuBar(true);

        Menu debugMenu = new Menu("Debug");
        MenuItem showConsole = new MenuItem("Show Console");

        showConsole.setOnAction(event -> {
            final WebView webView = new WebView();
            final WebEngine webEngine = webView.getEngine();
            final StringBuilder content = new StringBuilder();

            for (StreamHandler.WrappedString line : StreamHandler.lastLines)
                content.append(HTMLUtil.toHtml(line));
            webEngine.loadContent("<html><body><pre>" + content.toString() + "</pre></body></html>");

            Consumer<StreamHandler.WrappedString> consumer = line -> {
                    content.append(HTMLUtil.toHtml(line));
                    webEngine.loadContent("<html><body><pre>" + content.toString() + "</pre></body></html>");
            };
            final Runnable removeOutput = StreamHandler.getOutputStream().onLine(consumer);
            final Runnable removeError = StreamHandler.getErrorStream().onLine(consumer);

            System.out.println("Opening console");

            Dialog dialog = new Dialog();
            dialog.setTitle("Console");
            dialog.getDialogPane().setContent(webView);
            dialog.getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.OK_DONE));
            dialog.setResizable(true);
            dialog.initModality(Modality.NONE);
            dialog.show();


            dialog.setOnHidden(e -> {
                removeOutput.run();
                removeError.run();
                System.out.println("Closed console");
            });

            System.out.println("Opened console");
        });

        debugMenu.getItems().addAll(showConsole);

        menubar.getMenus().add(debugMenu);

        return menubar;
    }

}
