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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Accessors(chain = true)
public class Tree<M> {

    @Setter
    private Tree<M> root;

    @Setter
    private M leaf;
    private Set<Tree<M>> branches = new HashSet<>();

    public Tree(Tree<M> root, M leaf){
        this.root = root;
        this.leaf = leaf;
    }

    public Tree(M leaf){
        this.root = null;
        this.leaf = leaf;
    }

    public Tree(Tree<M> root){
        this.root = root;

        if(root != null){
            root.addBranch(this);
        }
    }

    public Tree<M> addBranch(M branch){
        Tree<M> tree = new Tree<>(this).setLeaf(branch);
        branches.add(tree);
        return tree;
    }

    public void addBranch(Tree<M> branch){
        branches.add(branch);
    }

    public boolean contains(M leaf){
        if(leaf == null){
            return false;
        }

        Set<Tree<M>> seen = new HashSet<>();
        Queue<Tree<M>> resolve = new LinkedList<>();
        resolve.add(this);

        while(!resolve.isEmpty()){
            Tree<M> check = resolve.remove();

            if(!seen.add(check)){
                continue;
            }

            if(leaf.equals(check.getLeaf())){
                return true;
            }

            for(Tree<M> tree : check.getBranches()){
                if(!seen.contains(tree) && !resolve.contains(tree)){
                    resolve.add(tree);
                }
            }

            if(check.getRoot() != null && !seen.contains(check.getRoot()) && !resolve.contains(check.getRoot())){
                resolve.add(check.getRoot());
            }
        }

        return false;
    }

    public Tree<M> clone(){
        return clone(new HashMap<>());
    }

    private Tree<M> clone(HashMap<Tree<M>, Tree<M>> ignored){
        if(ignored.containsKey(this)){
            return ignored.get(this);
        }

        Tree<M> tree = new Tree<>(leaf);
        ignored.put(this, tree);

        for(Tree<M> branch : branches){
            tree.addBranch(branch.clone(ignored));
        }

        return tree;
    }

    @Override
    public String toString(){
        return toString(new ArrayList<>());
    }

    private String toString(ArrayList<Tree<M>> ignored){
        if(ignored.contains(this)){
            return "Tree(" + leaf + ", ...)";
        }

        ignored.add(this);

        String branchString = "";
        for(Tree<M> branch : branches){
            if(!branchString.equals("")){
                branchString += ", ";
            }

            branchString += branch.toString(new ArrayList<>(ignored));
        }

        return "Tree(" + leaf + ", [" + branchString + "])";
    }

    /**
     * Gets this tree's depth.
     *
     * That is, the number of root nodes before a null root,
     * or this tree (the latter only happens in the case of
     * a circular tree).
     *
     * @return The depth.
     */
    public int depth() {
        Tree<M> topRoot = this;

        int depth = 0;
        while(topRoot.getRoot() != null && topRoot.getRoot() != this){
            depth++;
            topRoot = topRoot.getRoot();
        }

        return depth;
    }
    
    public int count(M object){
        return count(object, new ArrayList<>());
    }
    
    private int count(M object, ArrayList<Tree<M>> ignored){
        if(ignored.contains(this)){
            return 0;
        }
        
        ignored.add(this);
        
        int sum = leaf.equals(object) ? 1 : 0;
        for(Tree<M> branch : branches){
            sum += branch.count(object, new ArrayList<>(ignored));
        }
        
        return sum;
    }

    public Set<Tree<M>> flatten(){
        Set<Tree<M>> seen = new HashSet<>();
        Set<Tree<M>> flat = new HashSet<>();
        Queue<Tree<M>> resolve = new LinkedList<>();
        resolve.add(this);

        while(!resolve.isEmpty()){
            Tree<M> check = resolve.remove();

            if(!seen.add(check)){
                continue;
            }

            if(!flat.add(check)){
                System.out.println("flat.contains(" + check + ")");
                continue;
            }

            for(Tree<M> tree : check.getBranches()){
                if(!flat.contains(tree) && !resolve.contains(tree)){
                    resolve.add(tree);
                }
            }

            if(check.getRoot() != null && !flat.contains(check.getRoot()) && !resolve.contains(check.getRoot())){
                resolve.add(check.getRoot());
            }
        }

        return flat;
    }

    public Set<Tree<M>> flattenUnique(){
        HashMap<M, Tree<M>> flat = new HashMap<>();
        Queue<Tree<M>> resolve = new LinkedList<>();
        resolve.add(this);

        while(!resolve.isEmpty()){
            Tree<M> check = resolve.remove();

            if(check.getLeaf() != null && !flat.containsKey(check.getLeaf())){
                flat.put(check.getLeaf(), check);
            }

            for(Tree<M> tree : check.getBranches()){
                if(!flat.containsKey(tree.getLeaf()) && !resolve.contains(tree)){
                    resolve.add(tree);
                }
            }

            if(check.getRoot() != null && !flat.containsKey(check.getRoot().getLeaf()) && !resolve.contains(check.getRoot())){
                resolve.add(check.getRoot());
            }
        }

        return new HashSet<>(flat.values());
    }

    public static <M> Tree<M> join(Tree<M>... trees) {
        return join(Arrays.asList(trees));
    }

    public static <M> Tree<M> join(Iterable<Tree<M>> trees) {
        Tree<M> newRoot = new Tree<>((M) null);
        for (Tree<M> tree : trees) {
            tree.setRoot(newRoot);
            newRoot.addBranch(tree);
        }
        return newRoot;
    }

    public String prettyPrint(){
        return prettyPrint(null);
    }

    public String prettyPrint(Function<M, String> toString){
        return prettyPrint("", new ArrayList<>(), toString);
    }

    private String prettyPrint(String prefix, ArrayList<Tree<M>> ignored, Function<M, String> toString){
        if (toString == null)
            toString = M::toString;

        if(ignored.contains(this)){
            return prefix + " <-- ";
        }

        ignored.add(this);

        String newPrefix = "│  ";

        String branchString = "";
        int i = 0;
        for(Tree<M> branch : branches){
            i++;

            branchString += "\n";
            branchString += prefix;
            
            if(i == branches.size()){
                branchString += "└─ ";
                newPrefix = "   ";
            }else{
                branchString += "├─ ";
            }

            branchString += branch.prettyPrint(prefix + newPrefix, new ArrayList<>(ignored), toString);
        }

        String response = toString.apply(leaf);
        response += branchString;

        return response;
    }

    public Tree<M> generateHighestDependencyTree(){
        return generateHighestDependencyTree(null, new ArrayList<>(flattenUnique().stream().filter(b -> getTopRoot().count(b.getLeaf()) > 1).collect(Collectors.toList())));
    }

    private Tree<M> generateHighestDependencyTree(Tree<M> parent, ArrayList<Tree<M>> ignored){
        if(ignored.contains(this)){
            return null;
        }

        Tree<M> newParent = new Tree<>(parent, leaf);

        if(parent != null){
            parent.addBranch(newParent);
        }

        boolean hasIgnored = ignored.size() > 1;

        ignored.add(this);

        for(Tree<M> branch : branches){
            branch.generateHighestDependencyTree(newParent, new ArrayList<>(ignored));
        }

        if(hasIgnored && parent == null){
            for(Tree<M> tree : flattenUnique().stream().filter(b -> count(b.getLeaf()) > 1).collect(Collectors.toList())){
                newParent.addBranch(tree.getLeaf());
            }
        }

        return newParent;
    }

    public Tree<M> getTopRoot(){
        Tree<M> topRoot = this;

        while(topRoot.getRoot() != null && topRoot.getRoot() != this){
            topRoot = topRoot.getRoot();
        }

        return topRoot;
    }

    public List<Tree<M>> flattenAll(){
        return flattenAll(new ArrayList<>());
    }

    private List<Tree<M>> flattenAll(List<Tree<M>> ignored){
        if(ignored.contains(this)){
            return Collections.emptyList();
        }

        ignored.add(this);

        List<Tree<M>> elements = new ArrayList<>();
        for(Tree<M> branch : branches){
            elements.addAll(branch.flattenAll(new ArrayList<>(ignored)));
        }

        elements.add(this);

        return elements;
    }
    
    
    public int size(){
        return flatten().size();
    }
    
}
