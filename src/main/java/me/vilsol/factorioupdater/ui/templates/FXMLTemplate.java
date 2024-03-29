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
package me.vilsol.factorioupdater.ui.templates;

import javafx.fxml.FXMLLoader;

import java.io.IOException;

public abstract class FXMLTemplate<T> {
    
    private final String templateName;
    private final T pane;
    
    public FXMLTemplate(String templateName){
        this.templateName = templateName;
    
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/templates/" + templateName + ".fxml"));
        loader.setController(this);
        
        try{
            pane = loader.load();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
    
    public T getPane(){
        return pane;
    }
    
}
