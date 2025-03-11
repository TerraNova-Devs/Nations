package de.terranova.nations.professions;

public enum ProfessionStatus {
    LOCKED,      // Noch gesperrt
    AVAILABLE,   // Erfüllt alle Voraussetzungen, kann ausgewählt werden
    ACTIVE,      // Derzeit im Fokus, man grindet dafür
    PAUSED,      // Grind war mal gestartet, aber jetzt pausiert
    COMPLETED    // Freigeschaltet (fertig)
}
