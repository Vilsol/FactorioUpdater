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
package me.vilsol.factorioupdater.old;

public class Updater {

    /*
    private HashMap<String, Version> toDownload = new HashMap<>();
    private HashMap<String, Version> latestVersions = new HashMap<>();
    private static final Pattern dependencyMatcher = Pattern.compile("(\\??)\\s?([a-zA-Z0-9-_]+)\\s?([>=<]*)\\s?(\\S*)");
    private List<Mod> installedMods;
    private File modFolder;
    private UserAgent agent;
    private int indent = 0;
    private List<String> exclude;
    private JFrame frame;
    private TextAreaOutputStream taOutputStream;
    private ActionListener actionListener;
    private String username;
    private String password;

    public Updater(boolean nogui) throws Exception {
        if(!nogui){
            JTextArea textArea = new JTextArea(20, 40);
            textArea.setFocusable(false);

            taOutputStream = new TextAreaOutputStream(textArea, "Updater");
            System.setOut(new PrintStream(taOutputStream));

            JButton loginButton = new JButton("Login");
            JTextField usernameText = new JTextField(16);
            JPasswordField passwordText = new JPasswordField(16);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.insets = new Insets(5, 3, 5, 3);
            c.gridx = 0; c.gridy = 0;
            panel.add(new JLabel("Username: "), c);

            c.gridx = 1; c.gridy = 0;
            panel.add(usernameText, c);

            c.gridx = 2; c.gridy = 0;
            panel.add(new JLabel("Password: "), c);

            c.gridx = 3; c.gridy = 0;
            panel.add(passwordText, c);

            c.gridx = 4; c.gridy = 0;
            panel.add(loginButton, c);

            c.insets = new Insets(0, 0, 0, 0);
            c.gridx = 0; c.gridy = 1; c.gridwidth = 5;
            panel.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), c);

            frame = new JFrame("Factorio Updater");
            frame.setContentPane(panel);
            frame.setResizable(false);
            frame.setSize(new Dimension(500, 350));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            System.out.println("Please login above!");

            actionListener = actionEvent -> new Thread(() -> {
                UserAgent agent = new UserAgent();

                loginButton.removeActionListener(actionListener);

                username = usernameText.getText();
                password = passwordText.getText();

                try {
                    System.out.println("Logging In...");

                    agent.visit("https://mods.factorio.com/login");
                    agent.doc.getForm(0).setTextField("username", username);
                    agent.doc.getForm(0).setPassword("password", password);
                    agent.doc.getForm(0).submit();

                    if (!agent.doc.outerHTML().contains("Invalid username or password")) {
                        System.out.println("Successfully logged in!");
                        update(agent);
                    }else{
                        System.out.println("Login failed, please retry!");
                        loginButton.addActionListener(actionListener);
                    }

                } catch (Exception e) {
                    System.out.println("Login failed, please retry!");
                    loginButton.addActionListener(actionListener);
                }
            }).start();

            loginButton.addActionListener(actionListener);
        }else{
            UserAgent agent = new UserAgent();

            Scanner sc = new Scanner(System.in);

            System.out.println("Username: ");
            String username = sc.nextLine();
            System.out.println("Password: ");
            String password = readPwd();
            System.out.println();

            System.out.println("Logging In...");

            agent.visit("https://mods.factorio.com/login");
            agent.doc.getForm(0).setTextField("username", username);
            agent.doc.getForm(0).setPassword("password", password);
            agent.doc.getForm(0).submit();

            System.out.println("Successfully logged in!");

            update(agent);
        }
    }

    public void update(UserAgent agent) throws Exception {
        this.agent = agent;

        File configFile = new File("config.json");

        if (!configFile.exists()) {
            System.out.println();
            System.out.println("Config file not found, creating one!");
            Files.copy(Updater.class.getResourceAsStream("/config.json"), configFile.toPath());
            System.out.println("Config file created, please populate and re-run!");
            return;
        }

        JSONObject config = new JSONObject(String.join("", Files.readAllLines(configFile.toPath())));

        modFolder = new File(config.getString("mod-path"));
        if (!modFolder.exists()) {
            System.err.println("Mod folder not found!");
            return;
        }

        if (!modFolder.isDirectory()) {
            System.err.println("Mod path is not a folder!");
            return;
        }

        if (!modFolder.canRead() || !modFolder.canWrite()) {
            System.err.println("No permission on mod folder!");
            return;
        }

        exclude = new ArrayList<>();
        JSONArray configExclude = config.getJSONArray("exclude");
        for (int i = 0; i < configExclude.length(); i++) {
            exclude.add(configExclude.getString(i));
        }

        File[] list = modFolder.listFiles();
        installedMods = new ArrayList<>();

        if (list != null) {
            for (File mod : list) {
                if (mod.getPath().endsWith(".zip")) {
                    String owner = new String((byte[]) Files.getAttribute(mod.toPath(), "user:mod_owner"));
                    String name = new String((byte[]) Files.getAttribute(mod.toPath(), "user:mod_name"));
                    Version version = new Version(new String((byte[]) Files.getAttribute(mod.toPath(), "user:mod_version")));

                    installedMods.add(new Mod(owner, name, version));
                }
            }
        }

        JSONObject mods = config.getJSONObject("mods");
        mods.keySet().forEach(mod -> getMod(mod.split("/")[0], mod.split("/")[1], new Version(mods.getString(mod))));

        System.out.println();

        if(toDownload.size() > 0){
            double fileSize = toDownload.values().stream().mapToInt(Version::getSize).sum();
            System.out.println("Need to download " + toDownload.size() + " mods (" + (new DecimalFormat("#.##").format(fileSize / 1000000d)) + " MB)");
            System.out.println();
        }

        toDownload.forEach((key, value) -> {
            if(exclude.contains(key)){
                System.out.println("Not downloading " + key + " (in exclude list)");
                return;
            }

            System.out.println("Downloading " + key + " " + value.toPrettyString());

            String owner = key.split("/")[0];
            String name = key.split("/")[1];

            Path path = Paths.get(modFolder.getAbsolutePath() + File.separator + value.getFileName());

            try {
                if(saveFile("https://mods.factorio.com" + value.getDownloadUrl(), path)) {
                    Files.setAttribute(path, "user:mod_owner", Charset.defaultCharset().encode(owner));
                    Files.setAttribute(path, "user:mod_name", Charset.defaultCharset().encode(name));
                    Files.setAttribute(path, "user:mod_version", Charset.defaultCharset().encode(value.toFileString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        });

        if(toDownload.size() > 0){
            System.out.println();
        }

        System.out.println("You are up to date!");
    }

    public String getUrl(String url) throws IOException {
        URL oracle = new URL(url);
        BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

        String whole = "";
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            whole += inputLine;
        }

        in.close();

        return whole;
    }

    public void getMod(String owner, String name, Version version) {
        System.out.println();
        System.out.println(indent(indent) + "Looking for " + owner + "/" + name + " (" + version.toPrettyString() + ")");

        if(toDownload.containsKey(owner + "/" + name)){
            boolean matches = toDownload.get(owner + "/" + name).matches(version);

            if(version.getSign() != null && version.getSign().equals(">=")){
                matches = toDownload.get(owner + "/" + name).newerOrEqualTo(version);
            }

            if(matches){
                System.out.println(indent(indent) + "Cache " + toDownload.get(owner + "/" + name).toPrettyString());
                return;
            }
        }else if(latestVersions.containsKey(owner + "/" + name) && latestVersions.get(owner + "/" + name).equals(version)){
            System.out.println(indent(indent) + "Already at latest version!");
            return;
        }

        String document = null;
        try {
            document = getUrl("https://mods.factorio.com/api/mods/" + name);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        Mod data = new Mod(new JSONObject(document));

        Version finalVersion = version;
        for (Version v : data.getVersions()) {
            if(!latestVersions.containsKey(owner + "/" + name)){
                latestVersions.put(owner + "/" + name, v);
            }

            boolean matches = v.matches(version);

            if(version.getSign() != null && version.getSign().equals(">=")){
                matches = v.newerOrEqualTo(version);
            }

            if (matches) {
                finalVersion = v;

                Optional<Mod> installed = installedMods.stream()
                        .filter(m -> m.getName().equals(name))
                        .findAny();

                if(installed.isPresent()){
                    if(installed.get().getVersions().get(0).equals(v)){
                        System.out.println(indent(indent) + "Already at latest version!");
                        break;
                    }
                }

                System.out.println(indent(indent) + "Found " + v.toPrettyString());
                toDownload.put(owner + "/" + name, v);
                break;
            }
        }

        if (finalVersion.getDependencies() != null) {
            indent++;

            finalVersion.getDependencies().forEach(d -> {
                Matcher matcher = dependencyMatcher.matcher(d);

                if (!matcher.find()) {
                    return;
                }

                boolean optional = matcher.group(1).equals("?");
                String dependencyName = matcher.group(2);
                String comparison = matcher.group(3);
                String dependencyVersion = matcher.group(4);

                Version requiredVersion = new Version();
                if (dependencyVersion != null && !dependencyVersion.isEmpty()) {
                    requiredVersion = new Version(dependencyVersion);
                    requiredVersion.optional(optional);

                    if (comparison != null && !comparison.isEmpty()) {
                        requiredVersion.sign(comparison);
                    }
                }

                try {
                    String search = getUrl("https://mods.factorio.com/api/mods?q=" + dependencyName + "&tags=&order=updated&page_size=25&page=1");
                    JSONObject found = new JSONObject(search);
                    JSONArray results = found.getJSONArray("results");
                    if (results.length() > 0) {
                        JSONObject json = (JSONObject) results.get(0);

                        if(exclude.contains(json.getString("owner") + "/" + json.getString("name"))) {
                            if (!requiredVersion.isOptional()) {
                                System.err.println("A non-optional dependency is in exclude list (" + json.getString("owner") + "/" + json.getString("name") + ")!");
                                System.exit(0);
                            }

                            return;
                        }

                        getMod(json.getString("owner"), json.getString("name"), requiredVersion);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }

            });
            indent--;
        }

    }

    public boolean saveFile(String url, Path path) throws Exception {
        HttpResponse httpResponse = agent.sendHEAD(url);
        try {
            if(httpResponse.getHeader("Location").endsWith("/login")){
                System.err.println("Re-Logging");

                agent.visit(httpResponse.getHeader("Location"));
                agent.doc.getForm(0).setTextField("username", username);
                agent.doc.getForm(0).setPassword("password", password);
                agent.doc.getForm(0).submit();

                httpResponse = agent.sendHEAD(url);

                if(httpResponse.getHeader("Location").endsWith("/login")){
                    System.err.println("Fatal error, cannot login!");
                    System.err.println("Please download manually: " + url);
                    return false;
                }
            }

            System.err.println("Downloading " + httpResponse.getHeader("Location"));

            agent.download(httpResponse.getHeader("Location"), path.toFile());
        }catch (Exception e){
            e.printStackTrace();
            path.toFile().delete();
            System.exit(0);
        }

        return true;
    }

    private static String readPwd() {
        Console c = System.console();
        if (c == null) {
            InputStream in = System.in;
            int max = 50;
            byte[] b = new byte[max];

            int l = 0;
            try {
                l = in.read(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            l--;
            if (l > 0) {
                byte[] e = new byte[l];
                System.arraycopy(b, 0, e, 0, l);
                return new String(e);
            } else {
                return null;
            }
        } else {
            return new String(c.readPassword());
        }
    }

    public String indent(int amount){
        String indent = "";
        for (int i = 0; i < amount * 4; i++) {
            indent += " ";
        }
        return indent;
    }
    */

}
