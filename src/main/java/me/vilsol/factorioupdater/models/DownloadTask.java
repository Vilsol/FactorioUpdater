package me.vilsol.factorioupdater.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.function.Consumer;

@Data
@AllArgsConstructor
public class DownloadTask {
    
    private final String name;
    private final String url;
    private final File target;
    private final long size;
    private final Consumer<DownloadTask> onComplete;
    
}