package de.terranova.nations.heroicmap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LatestTest {

  @Test
  void ofTwoQuickSnapshotsOnlyTheNewerIsAppliedAndOnlyOnce() {
    Latest<String> latest = new Latest<>();
    List<String> applied = new ArrayList<>();

    latest.offer("alt");
    latest.offer("neu");
    latest.applyNewest(applied::add);
    // a second task, started for "alt", finds nothing left
    latest.applyNewest(applied::add);

    assertEquals(List.of("neu"), applied);
  }

  @Test
  void aSnapshotOfferedWhileOneIsAppliedComesNext() {
    Latest<String> latest = new Latest<>();
    List<String> applied = new ArrayList<>();

    latest.offer("eins");
    latest.applyNewest(
        s -> {
          applied.add(s);
          latest.offer("zwei");
        });
    latest.applyNewest(applied::add);

    assertEquals(List.of("eins", "zwei"), applied);
  }
}
