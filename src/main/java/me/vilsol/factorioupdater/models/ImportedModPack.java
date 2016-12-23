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

import lombok.Data;

import java.io.File;
import java.util.Map;
import java.util.Set;

@Data
public class ImportedModPack extends ModPack {
    
    private final Set<String> invalidMods;
    private final Map<Mod, Version> invalidVersions;
    
    public ImportedModPack(String name, File directory, Version version, Version factorioVersion, Map<String, ModWithRelease> mods, Set<String> invalidMods, Map<Mod, Version> invalidVersions){
        super(name, directory, version, factorioVersion, mods);
        
        this.invalidMods = invalidMods;
        this.invalidVersions = invalidVersions;
    }
    
}
