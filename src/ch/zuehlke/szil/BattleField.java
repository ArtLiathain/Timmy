package ch.zuehlke.szil;

public class BattleField {

  private final double battleFieldHeight;
  private final double battleFieldWidth;
  private final double sentryBorderSize;

  public BattleField(double battleFieldHeight, double battleFieldWidth, int sentryBorderSize) {
    this.battleFieldHeight = battleFieldHeight;
    this.battleFieldWidth = battleFieldWidth;
    this.sentryBorderSize = sentryBorderSize * 1.3;
  }

  public boolean isInBattlefield(double x, double y) {
    return x > sentryBorderSize && x < battleFieldWidth - sentryBorderSize && y > sentryBorderSize && y < battleFieldHeight - sentryBorderSize;
  }


}
