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
package me.vilsol.factorioupdater;

import me.vilsol.factorioupdater.models.ModRelease;
import me.vilsol.factorioupdater.models.Tree;
import me.vilsol.factorioupdater.models.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModManagerTest {
    
    @Test
    public void testFetchTree(){
        ModManager.FetchTreeResult foo_bar = ModManager.getInstance().fetchTree("foo_bar", new Version("*"), ">=");

        foo_bar.getResult().flattenUnique().forEach(branch -> {
            if(branch == null || branch.getLeaf() == null){
                return;
            }

            branch.getLeaf().getDependencies().forEach(dependency -> {
                if(dependency.getName().equals("base") || foo_bar.getMissing().contains(dependency)){
                    return;
                }

                boolean contains = false;

                for(Tree<ModRelease> sub : branch.getBranches()){
                    if(sub.getLeaf().getModName().equals(dependency.getName())){
                        contains = true;
                        break;
                    }
                }

                assertTrue("Mod " + branch.getLeaf().getModName() + " did not contain " + dependency.getName(), contains);
            });
        });

        assertEquals(4, foo_bar.getMissing().size());
        assertEquals(54, foo_bar.getResult().flattenUnique().size());

        System.out.println(foo_bar.getResult().generateHighestDependencyTree().prettyPrint(r -> r.getModName() + "@" + r.getVersion() + " -> " + r.getDependencies()));
    }

    @Test
    public void testFetch(){
        ModManager.getInstance().fetchMod("foo_bar");
    }

}
