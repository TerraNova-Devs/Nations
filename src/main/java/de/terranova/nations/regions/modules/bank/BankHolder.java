package de.terranova.nations.regions.modules.bank;

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

public interface BankHolder {

  Bank getBank();

  @ApiStatus.OverrideOnly
  default void onTransaction(String record, int credit) {}

  UUID getId();
}
