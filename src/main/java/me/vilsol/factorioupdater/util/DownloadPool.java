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

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.vilsol.factorioupdater.managers.APIManager;
import me.vilsol.factorioupdater.models.DownloadTask;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public class DownloadPool {

    public DownloadPool(List<DownloadTask> tasks, Consumer<DownloadProgress> observer, Runnable completion){
        Thread listener = new Thread(() -> {
            DownloadProgress progress = new DownloadProgress(tasks.size(), tasks.stream().mapToLong(DownloadTask::getSize).sum());
            for(int i = 0; i < tasks.size(); i++){
                DownloadTask task = tasks.get(i);
                progress.currentSize = task.getSize();
                progress.currentDlSize = 0;
                progress.currentName = task.getName();
                observer.accept(progress);
    
                try{
                    String url = task.getUrl();
                    HttpResponse response;
                    
                    do{
                        response = APIManager.getInstance().getAgent().execute(new HttpHead(url));
                        
                        if(!response.containsHeader("Location")){
                            break;
                        }
                        
                        url = response.getFirstHeader("Location").getValue();
    
                        if(url.contains("login")){
                            System.out.println("Re-Logging in");
                            APIManager.getInstance().reLogin();
                        }
                    }while(response.containsHeader("Location"));
    
                    response = APIManager.getInstance().getAgent().execute(new HttpGet(url));
                    InputStream input = response.getEntity().getContent();
                    FileOutputStream output = new FileOutputStream(task.getTarget());
    
                    int c = 1;
                    byte[] buf = new byte[8192];
                    while (c > 0) {
                        c = input.read(buf, 0, buf.length);
                        
                        if (c <= 0) {
                            break;
                        }
                        
                        output.write(buf, 0, c);
                        progress.currentDlSize += c;
                        progress.totalDlSize += c;
                        observer.accept(progress);
                    }
                    
                    output.close();
                    input.close();
                    
                    if(task.getOnComplete() != null){
                        task.getOnComplete().accept(task);
                    }
                    
                    progress.currentDlSize = task.getSize();
                    observer.accept(progress);
                }catch(Exception e){
                    task.getTarget().delete();
                    throw new RuntimeException(e);
                }
                
                progress.totalDlCount++;
            }
    
            completion.run();
        });
    
        listener.start();
    }
    
    @Getter
    @RequiredArgsConstructor
    @ToString
    public static class DownloadProgress {
        
        @NonNull
        private int totalCount;
        private int totalDlCount = 0;
        
        @NonNull
        private long totalSize;
        private long totalDlSize = 0;
    
        private String currentName = null;
        private long currentSize = 0;
        private long currentDlSize = 0;
        
    }
    
}
