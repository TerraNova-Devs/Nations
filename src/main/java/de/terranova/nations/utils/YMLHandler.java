package de.terranova.nations.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class YMLHandler {

    public FileConfiguration modifyFile;
    File file;
    private File datafolder;
    private String name;

    public YMLHandler(String name, InputStream datafolder) throws IOException {
        modifyFile = YamlConfiguration.loadConfiguration(new InputStreamReader(datafolder));
    }

    public YMLHandler(String name, File datafolder) throws IOException {
        this.datafolder = datafolder;
        this.name = name;
        loadYAML();
    }

    public void loadYAML() {
        try {
            file = new File(datafolder, name);
            file.createNewFile();
            modifyFile = YamlConfiguration.loadConfiguration(file);
        } catch (IOException e) {
            System.out.println();
        }
    }

    public void unloadYAML() {
        try {
            modifyFile.save(file);
        } catch (IOException e) {
            System.out.println();
        }
    }

    public void reloadYAML() {
        try {
            modifyFile.save(file);
            loadYAML();
        } catch (IOException e) {
            System.out.println();
        }
    }
}
