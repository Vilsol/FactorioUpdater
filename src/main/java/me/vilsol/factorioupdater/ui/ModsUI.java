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

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.Mod;
import me.vilsol.factorioupdater.models.SearchedMod;
import me.vilsol.factorioupdater.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Nick Robson
 */
public class ModsUI {

    public static Tab createModsTab(UpdaterUI.Transitioner transition) {
        BorderPane layout = new BorderPane();

        ScrollPane fill = new ScrollPane();
        fill.setPannable(false);
        fill.setFitToWidth(true);
        fill.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        TextField searchTextField = new TextField();
        searchTextField.setPromptText("Search for mods...");

        ChoiceBox<Integer> pageSizeChoice = new ChoiceBox<>();

        pageSizeChoice.setValue(10);
        pageSizeChoice.getItems().add(10);
        pageSizeChoice.getItems().add(15);
        pageSizeChoice.getItems().add(20);
        pageSizeChoice.getItems().add(25);

        AtomicInteger page = new AtomicInteger(1);

        Runnable doSearch = new Runnable() {
            @Override
            public void run() {
                Integer pageSize = pageSizeChoice.getValue();
                if (pageSize == null)
                    return;
                int pageCount = search(searchTextField.getText(), pageSize, page.get(), fill);
                layout.setBottom(createPageSelector(page, pageCount, this));
            }
        };

        searchTextField.setOnAction(e -> doSearch.run());

        HBox hbox = new HBox(10);
        hbox.getChildren().add(searchTextField);
        hbox.getChildren().add(pageSizeChoice);

        layout.setTop(hbox);
        layout.setCenter(fill);

        Tab tab = new Tab("Mods", layout);
        tab.setClosable(false);
        return tab;
    }

    private static Node createPageSelector(AtomicInteger page, int pageCount, Runnable doSearch) {
        HBox hbox = new HBox(10);
        hbox.setFillHeight(true);

        if (pageCount == -1)
            return hbox;

        int start = Math.max(1, page.get() - 2);
        int end = Math.min(pageCount, start + 4);

        Consumer<Integer> pageSelected = i -> {
            System.out.println("Getting page " + i);
            page.set(i);
            doSearch.run();
        };

        if (page.get() > 1) {
            Button prevButton = new Button("Previous");
            prevButton.setOnAction(e -> pageSelected.accept(page.get() - 1));
            HBox inset = new HBox(10, prevButton, new Text("..."));
            inset.setFillHeight(true);
            inset.setAlignment(Pos.BASELINE_CENTER);
            hbox.getChildren().add(inset);
        }

        if (start > 1) {
            Button buttonFirst = new Button("1");
            buttonFirst.setOnAction(e -> pageSelected.accept(1));
            HBox inset = new HBox(10, buttonFirst);
            if (start > 2)
                inset.getChildren().add(new Text("..."));
            inset.setFillHeight(true);
            inset.setAlignment(Pos.BASELINE_CENTER);
            hbox.getChildren().add(inset);
        }

        for (int i = start, j = end; i <= j; i++) {
            final int x = i;
            Button button = new Button(String.valueOf(x));
            button.setOnAction(e -> pageSelected.accept(x));
            if (x == page.get())
                button.setDisable(true);
            hbox.getChildren().add(button);
        }

        if (pageCount > end) {
            Button buttonLast = new Button(String.valueOf(pageCount));
            buttonLast.setOnAction(e -> pageSelected.accept(pageCount));
            HBox inset = new HBox(10, buttonLast);
            if (pageCount != end + 1)
                inset.getChildren().add(0, new Text("..."));
            inset.setFillHeight(true);
            inset.setAlignment(Pos.BASELINE_CENTER);
            hbox.getChildren().add(inset);
        }

        if (pageCount > 1) {
            Button nextButton = new Button("Next");
            nextButton.setOnAction(e -> pageSelected.accept(page.get() + 1));
            HBox inset = new HBox(10, new Text("..."), nextButton);
            inset.setFillHeight(true);
            inset.setAlignment(Pos.BASELINE_CENTER);
            hbox.getChildren().add(inset);
        }

        return hbox;
    }

    /**
     * Searches for mods, providing results.
     *
     * @param searchText The search text.
     * @param pageSize The number of items in every page.
     * @param page The page number.
     * @param fill The scroll pane to be filled with results.
     *
     * @return The number of pages.
     */
    private static int search(String searchText, int pageSize, int page, ScrollPane fill) {
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10));

        String query;
        try {
            query = URLEncoder.encode(searchText, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -1;
        }

        // https://mods.factorio.com/api/mods?q=&tags=&order=top&page_size=10&page=1
        String url = Resource.URL_FACTORIO_MODS_MOD;
        url += "?q=" + query;
        url += "&tags=";
        url += "&order=";
        url += "&page_size=" + pageSize;
        url += "&page=" + page;

        String response = Utils.fetchURL(url);
        JSONObject o = new JSONObject(response);

        JSONObject pagination = o.getJSONObject("pagination");
        JSONArray results = o.getJSONArray("results");

        System.out.println("Search results (" + results.length() + "):");

        int offset = 0;
        for (int i = 0, j = results.length(); i < j; i++) {
            JSONObject result = results.getJSONObject(i);
            SearchedMod mod = new SearchedMod(result);
            System.out.println("- " + mod);

            Text nameText = new Text(mod.getTitle() + " v" + mod.getLatestRelease().getVersion());
            nameText.setFont(Font.font("Arial", FontWeight.BOLD, 15));

            Text ownerText = new Text("by " + mod.getOwner());

            double width = Math.max(50, fill.getWidth() - 170);
            Text summaryText = new Text(mod.getSummary());
            summaryText.setWrappingWidth(width);

            String modpage;
            try {
                URI uri = new URI("https", "mods.factorio.com", "/mods/" + mod.getOwner() + "/" + mod.getName(), null);
                modpage = uri.toASCIIString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                continue;
            }

            Hyperlink homepageLink = new Hyperlink(mod.getHomepage());
            homepageLink.setOnAction(e -> {
                try {
                    System.out.println("Trying to open homepage:");
                    open(new URI(mod.getHomepage()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Hyperlink modpageLink = new Hyperlink(modpage);
            modpageLink.setOnAction(e -> {
                try {
                    System.out.println("Trying to open homepage:");
                    open(new URI(modpage));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            layout.add(nameText, 0, offset);
            layout.add(ownerText, 1, offset++);
            layout.add(summaryText, 0, offset++);
            if (!mod.getHomepage().trim().isEmpty())
                layout.add(homepageLink, 0, offset++);
            layout.add(modpageLink, 0, offset++);
            if (i != results.length() - 1) {
                layout.add(new Separator(), 0, offset);
                layout.add(new Separator(), 1, offset++);
            }
        }

        fill.setContent(layout);

        return pagination.getInt("page_count");
    }

    public static void open(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
