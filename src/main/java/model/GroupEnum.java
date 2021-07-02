package model;

import lombok.Getter;

public enum GroupEnum {

  FI("red", OrientationEnum.EX_GAUCHE),
  PCF("red", OrientationEnum.EX_GAUCHE),
  PS("HotPink", OrientationEnum.GAUCHE),
  EELV("HotPink", OrientationEnum.GAUCHE),
  LREM("Gold", OrientationEnum.CENTRE),
  LR("DodgerBlue", OrientationEnum.DROITE),
  RN("darkblue", OrientationEnum.EX_DROITE),
  PR("darkblue", OrientationEnum.EX_DROITE),
  JOURNALISTE("darkgreen", OrientationEnum.UNKNOWN),
  UNKNOWN("black", OrientationEnum.UNKNOWN);

  @Getter
  private String          color;
  @Getter
  private OrientationEnum orientation;

  GroupEnum(String color, OrientationEnum orientation) {
    this.color       = color;
    this.orientation = orientation;
  }

  public static GroupEnum findByAbbr(String abbr) {
    for (GroupEnum v : values()) {
      if (v.name().equals(abbr)) {
        return v;
      }
    }
    return null;
  }
}
