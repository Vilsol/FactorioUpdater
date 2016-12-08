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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mod {

    private final String owner;
    private final String name;
    private final List<Version> versions;

    public Mod(JSONObject json){
        owner = json.getString("owner");
        name = json.getString("name");
        versions = new ArrayList<>();

        //json.getJSONArray("releases").forEach(release -> versions.add(new Version((JSONObject) release)));

        Collections.sort(versions);
    }

    public Mod(String owner, String name, List<Version> versions) {
        this.owner = owner;
        this.name = name;
        this.versions = versions;

        Collections.sort(versions);
    }

    public Mod(String owner, String name, Version version) {
        this.owner = owner;
        this.name = name;
        this.versions = Collections.singletonList(version);
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public List<Version> getVersions() {
        return versions;
    }

    @Override
    public String toString() {
        return "Mod{" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", versions=" + versions +
                '}';
    }
}
