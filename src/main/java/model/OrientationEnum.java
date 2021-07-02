package model;

import lombok.Getter;

public enum OrientationEnum {

  EX_GAUCHE(-20, "Extrême gauche"),
  GAUCHE(-10, "Gauche"),
  CENTRE(0, "Centre"),
  DROITE(10, "Droite"),
  EX_DROITE(20, "Extrême droite"),
  UNKNOWN(0, "Inconnu");

  @Getter
  private int    value;
  @Getter
  private String label;

  OrientationEnum(int value, String label) {
    this.value = value;
    this.label = label;
  }

  public static OrientationEnum findByAbbr(String abbr) {
    for (OrientationEnum v : values()) {
      if (v.name().equals(abbr)) {
        return v;
      }
    }
    return null;
  }
}
