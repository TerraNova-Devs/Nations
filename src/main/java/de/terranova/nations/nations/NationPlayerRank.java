package de.terranova.nations.nations;

public enum NationPlayerRank {
  LEADER(1000),
  VICE_LEADER(100),
  COUNCIL(10),
  MEMBER(1);

  int weight;

  NationPlayerRank(int weight) {
    this.weight = weight;
  }

  public int getWeight() {
    return weight;
  }
}
