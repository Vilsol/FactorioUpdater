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

import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TreeTest {
    
    @Test
    public void testTree() throws Exception{
        Tree<String> A = new Tree<>("A");
        Tree<String> B = new Tree<>(A).setLeaf("B");
        Tree<String> C = new Tree<>(A).setLeaf("C");
        Tree<String> D = new Tree<>(B).setLeaf("D");
        Tree<String> E = new Tree<>(B).setLeaf("E");
        Tree<String> F = new Tree<>(C).setLeaf("F");
        Tree<String> G = new Tree<>(C).setLeaf("G");
        Tree<String> H = new Tree<>(D).setLeaf("H");

        E.addBranch("W");
        F.addBranch("X");
        G.addBranch("Y");

        assertTrue(A.contains("H"));
        assertTrue(H.contains("A"));
        assertFalse(A.contains("Z"));
        assertFalse(H.contains("Z"));
        assertNull(A.getRoot());
        assertEquals(2, A.getBranches().size());
        assertEquals(1, D.getBranches().size());
        assertEquals(0, H.getBranches().size());
        assertEquals("Tree(D, [Tree(H, [])])", D.toString());
        assertEquals(11, A.flatten().size());

        H.addBranch(A);

        assertTrue(A.contains("H"));
        assertTrue(H.contains("A"));
        assertFalse(A.contains("Z"));
        assertFalse(H.contains("Z"));
        assertNull(A.getRoot());
        assertEquals(2, A.getBranches().size());
        assertEquals(1, D.getBranches().size());
        assertEquals(1, H.getBranches().size());
        assertEquals(11, A.flatten().size());

        Tree<String> clone = A.clone();

        assertTrue(clone.contains("H"));
        assertTrue(clone.contains("A"));
        assertFalse(clone.contains("Z"));
        assertNull(clone.getRoot());
        assertEquals(11, clone.flatten().size());

        clone.addBranch("W");
        clone.addBranch("X");
        clone.addBranch("Y");

        assertEquals(14, clone.flatten().size());
        assertEquals(11, clone.flattenUnique().size());
    }
    
}
