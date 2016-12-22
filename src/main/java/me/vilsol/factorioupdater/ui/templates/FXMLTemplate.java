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
