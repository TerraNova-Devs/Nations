package de.terranova.nations.professions.pojo;

import java.util.List;

public class ProfessionConfig {
    public String professionId;
    public String type;
    public String prettyName;
    public String icon;
    public int level;
    public int score;
    public int price;
    public List<BuildingConfig> buildings;
    public List<ObjectiveConfig> objectives;


    public ProfessionConfig() {
    }

    public int getLevel() {
        return level;
    }
}

