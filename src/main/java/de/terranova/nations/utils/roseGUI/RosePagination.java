package de.terranova.nations.utils.roseGUI;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RosePagination {
  private final RoseGUI gui;
  private final LinkedList<Integer> slots = new LinkedList<>();
  private final LinkedList<RoseItem> items = new LinkedList<>();
  private int page;

  public RosePagination(RoseGUI gui) {
    this.gui = gui;
  }

  public List<Integer> getSlots() {
    return this.slots;
  }

  public void registerPageSlots(Integer... slots) {
    this.registerPageSlots(Arrays.asList(slots));
  }

  public void registerPageSlots(List<Integer> slots) {
    this.slots.addAll(slots);
  }

  public void registerPageSlotsBetween(int from, int to) {
    if (from > to) {
      registerPageSlotsBetween(to, from);
      return;
    }
    for (; from <= to; from++) {
      this.slots.add(from);
    }
  }

  public void unregisterAllPageSlots() {
    this.slots.clear();
  }

  public void clearAllItems() {
    this.items.clear();
  }

  public RoseGUI getGui() {
    return this.gui;
  }

  public int getCurrentPage() {
    return this.page;
  }

  public RosePagination setPage(int page) {
    this.page = page;
    return this;
  }

  public RosePagination goNextPage() {
    if (this.page >= this.getLastPage()) return this;
    this.page += 1;
    return this;
  }

  public RosePagination goPreviousPage() {
    if (this.page <= 0) return this;
    this.page -= 1;
    return this;
  }

  public RosePagination goFirstPage() {
    this.page = 0;
    return this;
  }

  public RosePagination goLastPage() {
    this.page = getLastPage();
    return this;
  }

  public boolean isLastPage() {
    return this.page == getLastPage();
  }

  public boolean isFirstPage() {
    return this.page == 0;
  }

  public int getLastPage() {
    if (this.slots.isEmpty() || this.items.isEmpty()) return 0;

    int division = (int) Math.floor(this.items.size() / this.slots.size());

    if (this.items.size() % this.slots.size() == 0) return division - 1;
    return division;
  }

  public void addItem(RoseItem... items) {
    this.items.addAll(Arrays.asList(items));
  }

  public List<RoseItem> getItems() {
    return this.items;
  }

  public void update() {
    if (this.page < 0) return;

    for (int slotNo = 0; slotNo < this.slots.size(); slotNo++) {
      int itemNo = slotNo + (this.page * this.slots.size());
      if (this.items.size() > itemNo) {
        this.gui.addItem(this.slots.get(slotNo), this.items.get(itemNo));
      } else {
        this.gui.addItem(null, this.slots.get(slotNo));
      }
    }
  }
}
