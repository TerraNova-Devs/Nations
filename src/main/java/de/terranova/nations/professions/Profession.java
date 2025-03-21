package de.terranova.nations.professions;

public class Profession {
    private final int professionId;
    private final String type;  // "FISHERY", "FARMING", ...
    private final int level;    // 1..4
    private final int score;    // Wie viel Score beim Freischalten
    private final int price;    // Wie viel Silber kostet Freischaltung

    public Profession(int professionId, String type, int level, int score, int price) {
        this.professionId = professionId;
        this.type = type;
        this.level = level;
        this.score = score;
        this.price = price;
    }

    public int getProfessionId() {
        return professionId;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }

    public int getPrice() {
        return price;
    }

    public static String prettyName(String type) {
        return switch (type) {
            case "FISHERY" -> "Fischerei";
            case "FARMING" -> "Landwirtschaft";
            case "MINING" -> "Bergbau";
            case "WOODCUTTING" -> "Holzfällerei";
            case "TRADING" -> "Handel";
            case "FIGHTING" -> "Kampf";
            default -> type;
        };
    }
}
