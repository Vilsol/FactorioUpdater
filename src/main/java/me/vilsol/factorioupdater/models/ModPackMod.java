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

import lombok.AllArgsConstructor;
import lombok.Data;
import me.vilsol.factorioupdater.util.Mappable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nick Robson
 */
@Data
@AllArgsConstructor
public class ModPackMod implements Mappable {

    private final Mod mod;
    private final ModRelease modRelease;

    private boolean enabled = true;

    public JSONObject getAsJson(){
        JSONObject result = new JSONObject();
        result.put("version", modRelease.getVersion().toString());
        result.put("enabled", enabled);
        return result;
    }
    
    @Override
    public Map<String, Object> map(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("mod", mod.map());
        map.put("modRelease", modRelease.map());
        return map;
    }
    
}
