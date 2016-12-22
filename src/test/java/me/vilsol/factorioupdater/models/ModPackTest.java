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
package me.vilsol.factorioupdater.models;

import com.jaunt.UserAgent;
import me.vilsol.factorioupdater.managers.ModManager;
import me.vilsol.factorioupdater.Resource;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ModPackTest {

    private ModPack modPack;

    @Before
    public void setupModpack() throws Exception {
        Mod mod = ModManager.getInstance().fetchMod("foo_bar");
        ModRelease mr = mod.matchRelease("=", new Version("1.0.0"));

        Map<String, ModWithRelease> mods = new HashMap<String, ModWithRelease>() {{
            put(mod.getName(), new ModWithRelease(mod, mr, true));
        }};

        File directory = new File(Resource.APP_MODPACK_DIR, "test_pack");
        Version version = new Version(1, 2, 3);
        Version factorioVersion = new Version("0.14");

        modPack = new ModPack("Test Pack", directory, version, factorioVersion, mods);

        File credentials = new File(Resource.APP_HOME_DIR, "credentials.json");
        String username = null;
        String password = null;

        if (credentials.exists()) {
            try {
                JSONObject o = new JSONObject(String.join("", Files.readAllLines(credentials.toPath())));
                username = o.optString("username");
                password = o.optString("password");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        assertNotNull(username);
        assertNotNull(password);

        UserAgent agent = new UserAgent();
        agent.visit("https://mods.factorio.com/login");
        agent.doc.getForm(0).setTextField("username", username);
        agent.doc.getForm(0).setPassword("password", password);
        agent.doc.getForm(0).submit();

        ModManager.getInstance().setAgent(agent);
    }

    @Test
    public void testExport(){
        assertEquals(modPack, ModPack.importPack(modPack.exportPack(), modPack.getRootDirectory()));
    }
    
    @Test
    public void testDownload(){
        modPack.download();
    }

    @After
    public void cleanupModpack(){
        modPack.getRootDirectory().delete();
    }

}
