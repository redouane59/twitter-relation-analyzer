package model;

import lombok.Getter;

public enum DirectionEnum {

  EX_GAUCHE(-2),
  GAUCHE(-1),
  CENTRE(0),
  DROITE(1),
  EX_DROITE(2),
  UNKNOWN(0);

  @Getter
  private int value;

  DirectionEnum(int value) {
    this.value = value;
  }

  public static DirectionEnum findByAbbr(String abbr) {
    for (DirectionEnum v : values()) {
      if (v.name().equals(abbr)) {
        return v;
      }
    }
    return null;
  }
}
