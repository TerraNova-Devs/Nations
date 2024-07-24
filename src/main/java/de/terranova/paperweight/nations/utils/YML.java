package de.terranova.paperweight.nations.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YML {

    public FileConfiguration modifyFile;
    File file;

    public YML(String name, InputStream datafolder) throws IOException {
        modifyFile = YamlConfiguration.loadConfiguration(new InputStreamReader(datafolder));
    }

    public YML(String name, File datafolder) throws IOException {
        file = new File(datafolder, name);
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        modifyFile = YamlConfiguration.loadConfiguration(file);
    }

    public void unloadYAML() {
        try {
            modifyFile.save(file);
        } catch (IOException e) {
            System.out.println();
        }
    }
}
