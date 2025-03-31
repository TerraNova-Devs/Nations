package de.terranova.nations.professions.pojo;


import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfessionConfigLoader {

    public static ProfessionsYaml load(File file) throws IOException {
        Yaml yaml = new Yaml();

        try (InputStream is = new FileInputStream(file)) {
            return yaml.loadAs(is, ProfessionsYaml.class);
        }
    }
}

