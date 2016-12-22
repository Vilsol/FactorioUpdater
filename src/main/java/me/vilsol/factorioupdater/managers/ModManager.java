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
package me.vilsol.factorioupdater.managers;

import com.jaunt.UserAgent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.vilsol.factorioupdater.Resource;
import me.vilsol.factorioupdater.models.*;
import me.vilsol.factorioupdater.util.Utils;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ModManager {

    private static final ModManager instance = new ModManager();

    @Getter
    @Setter
    private UserAgent agent = new UserAgent();
    private Map<String, Mod> modCache = new ConcurrentHashMap<>();
    private Map<String, FetchTreeResult> modTreeCache = new ConcurrentHashMap<>();

    private ModManager() {}

    public FetchTreeResult fetchTree(String name, Version version, String comparison){
        final String CACHE_KEY = name + comparison + version;
        if (modTreeCache.containsKey(CACHE_KEY))
            return modTreeCache.get(CACHE_KEY);
        ThreadFactory factory = r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            return t;
        };
        ThreadPoolExecutor  pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8, factory);
        Map<String, Dependency> seen = new ConcurrentHashMap<>();
        Map<String, Tree<ModWithRelease>> releaseTrees = new ConcurrentHashMap<>();
        Tree<ModWithRelease> result = new Tree<>((Tree<ModWithRelease>) null);
        CopyOnWriteArrayList<Dependency> missing = new CopyOnWriteArrayList<>();
        Map<Tree<ModWithRelease>, List<String>> laterBranch = new ConcurrentHashMap<>();
        CopyOnWriteArrayList<String> resolving = new CopyOnWriteArrayList<>();

        pool.submit(new FetchMod(pool, seen, releaseTrees, result, missing, laterBranch, resolving, new Dependency(name, false, version, comparison)));

        while(pool.getTaskCount() > pool.getCompletedTaskCount()){
            try{
                Thread.sleep(10);
            }catch(InterruptedException e){
                throw new RuntimeException(e);
            }
        }

        for(Map.Entry<Tree<ModWithRelease>, List<String>> later : laterBranch.entrySet()){
            for(String dep : later.getValue()){
                Tree<ModWithRelease> branch = releaseTrees.get(dep);
                if(branch != null){
                    later.getKey().addBranch(branch);
                }
            }
        }

        Tree<ModWithRelease> modRoot = result.getBranches().iterator().next();
        modRoot.setRoot(null);

        FetchTreeResult r =  new FetchTreeResult(modRoot, missing);
        modTreeCache.put(CACHE_KEY, r);
        return r;
    }

    public Mod fetchMod(String name){
        String response;

        try{
            response = Utils.fetchURL(Resource.URL_FACTORIO_MODS_MOD + URLEncoder.encode(name, "UTF-8"));
        }catch(UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }

        JSONObject data = new JSONObject(response);
        return new Mod(data);
    }

    public static ModManager getInstance(){
        return instance;
    }

    @AllArgsConstructor
    protected class FetchMod implements Runnable {

        private final ExecutorService pool;
        private final Map<String, Dependency> seen;
        private final Map<String, Tree<ModWithRelease>> releaseTrees;
        private final Tree<ModWithRelease> root;
        private final CopyOnWriteArrayList<Dependency> missing;
        private final Map<Tree<ModWithRelease>, List<String>> laterBranch;
        private final CopyOnWriteArrayList<String> resolving;
        private final Dependency check;

        @Override
        public void run(){
            if (seen.containsKey(check.getName())){
                return;
            }

            seen.put(check.getName(), check);

            Mod mod = modCache.computeIfAbsent(check.getName(), s -> {
                try{
                    return fetchMod(s);
                }catch(Exception e){
                    try{
                        System.err.println("No mod found with name '" + s + "' at " + Resource.URL_FACTORIO_MODS_MOD + URLEncoder.encode(check.getName(), "UTF-8"));
                    }catch(UnsupportedEncodingException e1){
                        throw new RuntimeException(e1);
                    }

                    missing.add(check);
                    throw new RuntimeException(e);
                }
            });

            ModRelease release = mod.matchRelease(check.getComparison(), check.getVersion());

            if (release == null) {
                System.out.println("Release not found for " + check.getName());
                throw new RuntimeException(String.format("Could not resolve dependency '%s' with version '%s' and constraint '%s'\n", check.getName(), check.getVersion(), check.getComparison()));
            }

            Tree<ModWithRelease> newRoot = root.addBranch(new ModWithRelease(mod, release, true));
            releaseTrees.put(check.getName(), newRoot);

            for(Dependency dependency : release.getDependencies()){
                if(dependency.getName().equals("base")){
                    continue;
                }

                if(!seen.containsKey(dependency.getName()) && !resolving.contains(dependency.getName())){
                    resolving.add(dependency.getName());
                    pool.submit(new FetchMod(pool, seen, releaseTrees, newRoot, missing, laterBranch, resolving, dependency));
                }else{
                    Tree<ModWithRelease> modReleaseTree = releaseTrees.get(dependency.getName());

                    if(modReleaseTree != null){
                        newRoot.addBranch(modReleaseTree);
                    }else{
                        laterBranch.computeIfAbsent(newRoot, m -> new ArrayList<>()).add(dependency.getName());
                    }

                    /*
                    Dependency previous = seen.get(dependency.getName());
                    Dependency lowest = Utils.getLowest(previous, dependency);
                    Dependency highest = lowest == previous ? dependency : previous;
                    if(!lowest.getVersion().matches("<=", highest.getVersion())){
                        // TODO shit the floor
                        throw new RuntimeException("Shit the floor: " + lowest + " is not >= " + highest + " of " + check.getName());
                    }
                    */
                }
            }
        }

    }

    @Data
    @AllArgsConstructor
    public class FetchTreeResult {

        private final Tree<ModWithRelease> result;
        private final List<Dependency> missing;

    }

}
