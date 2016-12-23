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
package me.vilsol.factorioupdater.util;

import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FuturePool<T> {

    private Thread listener;
    
    private List<Future<T>> futures = new CopyOnWriteArrayList<>();
    private List<Callback<List<T>, Void>> callbacks = new CopyOnWriteArrayList<>();
    private boolean started;
    
    private void restart(){
        if(!started){
            return;
        }
        
        if(listener != null){
            listener.interrupt();
        }
        
        listener = new Thread(() -> {
            try{
                List<T> list = new ArrayList<>(futures.size());
    
                for(Future<T> next : futures){
                    list.add(next.get());
                }
                
                callbacks.forEach(c -> c.call(list));
            }catch(InterruptedException | ExecutionException e){
                e.printStackTrace();
            }
        });
    
        listener.start();
    }
    
    public void addFuture(Future<T> future){
        if(!futures.contains(future)){
            futures.add(future);
            restart();
        }
    }
    
    public void onComplete(Callback<List<T>, Void> callback){
        if(!callbacks.contains(callback)){
            callbacks.add(callback);
        }
    }
    
    public void start(){
        started = true;
        restart();
    }
    
}
