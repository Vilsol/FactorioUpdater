package me.vilsol.factorioupdater.models;

import lombok.Data;

import java.io.File;
import java.util.Map;
import java.util.Set;

@Data
public class ImportedModPack extends ModPack {
    
    private final Set<String> invalidMods;
    private final Map<Mod, Version> invalidVersions;
    
    public ImportedModPack(String name, File directory, Version version, Version factorioVersion, Map<String, ModWithRelease> mods, Set<String> invalidMods, Map<Mod, Version> invalidVersions){
        super(name, directory, version, factorioVersion, mods);
        
        this.invalidMods = invalidMods;
        this.invalidVersions = invalidVersions;
    }
    
}
