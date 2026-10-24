package de.terranova.nations.regions.modules.access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AccessLevelTest {

  @Test
  void opsActAsAdminInEveryTown() {
    assertEquals(AccessLevel.ADMIN, AccessLevel.acting(true, null));
    assertEquals(AccessLevel.ADMIN, AccessLevel.acting(true, AccessLevel.TRUSTED));
    assertEquals(AccessLevel.ADMIN, AccessLevel.acting(true, AccessLevel.MAJOR));
  }

  @Test
  void othersActWithTheirOwnLevel() {
    assertNull(AccessLevel.acting(false, null));
    for (AccessLevel own : AccessLevel.values()) {
      assertEquals(own, AccessLevel.acting(false, own));
    }
  }
}
