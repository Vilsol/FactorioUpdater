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

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import me.vilsol.factorioupdater.managers.ModManager;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Nick Robson
 */
public class ModpacksUI {

    public static Tab createModPacksTab(UpdaterUI.Transitioner transition) {
        GridPane pane = new GridPane();
        pane.setVgap(5);
        pane.setPadding(new Insets(20));

        AtomicInteger row = new AtomicInteger(0);

        Map<File, ModPack> packsByDirectory = new ConcurrentHashMap<>();

        File packsDirectory = new File(Resource.APP_HOME_DIR, "modpacks");
        if (!packsDirectory.exists() && !packsDirectory.mkdir()) {
            UI.showAlert(Alert.AlertType.ERROR, null, "Failed to create modpacks directory.");
        }

        Runnable loadModPacks = () -> {
            row.set(0);
            pane.getChildren().clear();
            if (!packsDirectory.isDirectory())
                return;
            for (File packDir : packsDirectory.listFiles(f -> f.isDirectory())) {
                try {
                    ModPack pack = loadModPack(packDir);
                    if (pack == null) continue;
                    packsByDirectory.put(packDir, pack);
                    Node modPackUI = createModPackUI(transition, pack);
                    Platform.runLater(() -> pane.add(modPackUI, 0, row.getAndIncrement()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(loadModPacks, "Modpack Loader");
        thread.setDaemon(true);
        thread.start();

        thread = new Thread(() -> {
            if (!packsDirectory.isDirectory())
                return;
            try {
                Path packsPath = packsDirectory.toPath();
                WatchService watchService = packsPath.getFileSystem().newWatchService();
                WatchEvent.Kind[] kinds = { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY };
                packsPath.register(watchService, kinds);
                transition.onDestroy(() -> {
                    try {
                        watchService.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                WatchKey current;
                while (true) {
                    try {
                        current = watchService.poll(1, TimeUnit.SECONDS);
                        
                        if(current != null){
                            for (WatchEvent<?> watchEvent : current.pollEvents()) {
                                WatchEvent.Kind<?> kind = watchEvent.kind();
                                if (kind == StandardWatchEventKinds.OVERFLOW)
                                    continue;
                                WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
                                Path path = event.context();
                                Path fullPath = packsPath.resolve(path);
                                File file = fullPath.toFile();
                                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                    if (file.isDirectory() && fullPath.getParent().equals(packsPath)) {
                                        ModPack pack = loadModPack(file);
                                        if (pack == null) continue;
                                        packsByDirectory.put(file, pack);
                                    }
                                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                    if (file.isDirectory() && fullPath.getParent().equals(packsPath)) {
                                        packsByDirectory.remove(file);
                                    }
                                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                    while (fullPath.getParent() != null && !fullPath.getParent().equals(packsPath)) {
                                        fullPath = fullPath.getParent();
                                    }
                                    file = fullPath.toFile();
                                    if (file.isDirectory() && packsPath.equals(fullPath.getParent())) {
                                        ModPack pack = loadModPack(file);
                                        if (pack == null) continue;
                                        packsByDirectory.put(file, pack);
                                    }
                                }
                            }
                            loadModPacks.run();
                            boolean valid = current.reset();
                            if (!valid) {
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();

        Tab tab = new Tab("Modpacks", pane);
        tab.setClosable(false);
        return tab;
    }

    private static ModPack loadModPack(File packDir) {
        Map<String, File> files = Arrays.stream(packDir.listFiles()).collect(Collectors.toMap(f -> f.getName(), f -> f));
        File packJson = files.get("pack.json");
        if (packJson == null) {
            System.out.format("No pack.json in %s, assuming not a modpack instance\n", packDir);
            return null;
        }
        System.out.format("Found mod pack directory: %s\n", packDir);
        if (!packJson.isFile() || !packJson.canRead()) {
            UI.showAlert(Alert.AlertType.ERROR, null, "%s is not a file, or cannot be read!", packJson);
            return null;
        }
        JSONObject rootJson;
        try {
            List<String> lines = Files.readAllLines(packJson.toPath()).stream().map(String::trim).collect(Collectors.toList());
            rootJson = new JSONObject(String.join("", lines));
        } catch (IOException e) {
            UI.showAlert(Alert.AlertType.ERROR, null, "Failed to parse JSON in %s!", packJson);
            e.printStackTrace();
            return null;
        }
        String packName = rootJson.getString("name");
        String packVersionString = rootJson.getString("version");
        String packFactorioVersionString = rootJson.getString("factorio_version");
        Version packVersion;
        try {
            packVersion = new Version(packVersionString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Version packFactorioVersion;
        try {
            packFactorioVersion = new Version(packFactorioVersionString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        JSONObject modsJson = rootJson.getJSONObject("mods");
        Map<String, ModWithRelease> packMods = new HashMap<>();
        Set<String> invalidMods = new HashSet<>();
        Map<Mod, Version> invalidVersions = new HashMap<>();
        modsJson.keys().forEachRemaining(mod -> {
            JSONObject o = modsJson.optJSONObject(mod);
            if (o == null) {
                o = new JSONObject();
                o.put("version", modsJson.getString(mod));
            }
            String version = o.getString("version");
            boolean enabled = o.optBoolean("enabled", true);
            Mod m = ModManager.getInstance().fetchMod(mod);
            if (m == null) {
                invalidMods.add(mod);
                return;
            }
            Version v = new Version(version);
            ModRelease r = m.matchRelease("=", v);
            if (r == null) {
                invalidVersions.put(m, v);
            } else {
                packMods.put(m.getName(), new ModWithRelease(m, r, enabled));
            }
        });

        String reportString = "";
        if (!invalidMods.isEmpty()) {
            reportString += "There are no mods with these names:\n\t";
            reportString += String.join("\n\t", invalidMods);
        }
        if (!invalidVersions.isEmpty()) {
            if (!reportString.isEmpty())
                reportString += "\n\n";
            reportString += "There are no releases matching these versions:\n\t";
            reportString += String.join("\n\t", invalidVersions.entrySet().stream().map(e -> e.getKey().getName() + " @ " + e.getValue().toString()).collect(Collectors.toList()));
        }
        if (!reportString.isEmpty()) {
            UI.showAlert(Alert.AlertType.WARNING, null, reportString);
        }

        return new ModPack(packName, packDir, packVersion, packFactorioVersion, packMods);
    }

    private static Node createModPackUI(UpdaterUI.Transitioner transition, ModPack pack) {
        Text packName = new Text(pack.getName());
        packName.setFont(Font.font("Arial", 20));

        Text packVersion = new Text("v" + pack.getVersion().toString());
        packVersion.setFont(Font.font("Arial", 16));

        Button button = new Button("Configure");

        button.setOnAction(e -> {
            transition.transition(createConfigurePackScreen(transition, pack), false);
        });

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.BASELINE_LEFT);

        HBox hbox = new HBox(20);
        hbox.setFillHeight(true);
        hbox.setAlignment(Pos.BASELINE_LEFT);

        hbox.getChildren().add(packName);
        hbox.getChildren().add(packVersion);

        vbox.getChildren().add(hbox);
        vbox.getChildren().add(button);

        return vbox;
    }

    private static Parent createConfigurePackScreen(UpdaterUI.Transitioner transition, ModPack pack) {
        ScrollPane scrollPane = new ScrollPane();

        Thread thread = new Thread(() -> {
            GridPane rootPane = new GridPane();
            rootPane.setPadding(new Insets(20));
            rootPane.setAlignment(Pos.BASELINE_CENTER);
            rootPane.setVgap(30);
            rootPane.setHgap(20);

            GridPane modsPane = new GridPane();
            modsPane.setHgap(30);

            Text modsText = new Text("Mods");
            modsText.setFont(Font.font("Arial", 20));
            modsPane.add(modsText, 1, 0);

            GridPane missingModsPane = new GridPane();
            missingModsPane.setHgap(30);

            Text missingModsText = new Text("Missing Mods");
            missingModsText.setFont(Font.font("Arial", 20));
            missingModsPane.add(missingModsText, 0, 0);

            Map<String, ModWithRelease> mods = new HashMap<>(pack.getMods());
            Map<ModWithRelease, ModManager.FetchTreeResult> trees = mods.values().stream().collect(Collectors.toMap(m -> m, m -> ModManager.getInstance().fetchTree(m.getMod().getName(), m.getModRelease().getVersion(), "=")));

            AtomicInteger foundModsRow = new AtomicInteger(0);
            AtomicInteger missingModsRow = new AtomicInteger(0);
            trees.forEach((m, r) -> {
                Tree<ModWithRelease> tree = r.getResult().generateHighestDependencyTree();
                Stack<Tree<ModWithRelease>> stack = new Stack<>();
                stack.push(tree);
                while (!stack.isEmpty()) {
                    Tree<ModWithRelease> t = stack.pop();
                    ModRelease mr = t.getLeaf().getModRelease();
                    if (mr != null) {
                        int depth = t.depth();
                        String indent = repeat("\t", depth);
                        CheckBox checkBox = new CheckBox();
                        checkBox.setSelected(!mods.containsKey(mr.getModName()) || mods.get(mr.getModName()).isEnabled());
                        Text displayText = new Text(indent + mr.getModName() + " v" + mr.getVersion());
                        modsPane.add(checkBox, 0, foundModsRow.incrementAndGet());
                        modsPane.add(displayText, 1, foundModsRow.get());
                    }
                    for (Tree<ModWithRelease> branch : t.getBranches())
                        stack.push(branch);
                }

                for (Dependency dependency : r.getMissing()) {
                    missingModsPane.add(new Text(dependency.getName() + " " + (dependency.getVersion().matchesAny() ? "(any)" : dependency.getComparison() + " " + dependency.getVersion())), 0, missingModsRow.incrementAndGet());
                }
            });

            Button backButton = new Button("Back");
            backButton.setOnAction(e -> {
                if (transition.current().getRoot() == scrollPane)
                    transition.back(true);
            });

            VBox pane = new VBox(10);
            pane.getChildren().add(modsPane);
            if (missingModsRow.get() > 1)
                pane.getChildren().add(missingModsPane);
            pane.getChildren().add(backButton);

            Text modpackName = new Text(pack.getName());
            modpackName.setFont(Font.font("Arial", 36));

            Text modpackVersion = new Text("v" + pack.getVersion().toString());
            modpackVersion.setFont(Font.font("Arial", 24));

            HBox hbox = new HBox(5);
            hbox.setAlignment(Pos.BASELINE_LEFT);

            hbox.getChildren().add(modpackName);
            hbox.getChildren().add(modpackVersion);

            rootPane.add(hbox, 0, 0);
            rootPane.add(pane, 0, 1);

            Platform.runLater(() -> scrollPane.setContent(rootPane));
        });
        thread.setDaemon(true);
        thread.start();

        Text loadingText = new Text("Loading mod pack information...");
        loadingText.setFont(Font.font("Arial", 32));

        GridPane vbox = new GridPane();
        vbox.setAlignment(Pos.CENTER);
        vbox.add(loadingText, 0, 0);
        scrollPane.setContent(vbox);

        return scrollPane;
    }

    public static String repeat(String s, int times) {
        String r = "";
        for (int i = 0; i < times; i++)
            r += s;
        return r;
    }

}
