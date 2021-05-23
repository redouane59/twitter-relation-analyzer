import lombok.Getter;

public enum GroupEnum {

    FI("red", DirectionEnum.EX_GAUCHE),
    PCF("red", DirectionEnum.EX_GAUCHE),
    PS("HotPink", DirectionEnum.GAUCHE),
    EELV("HotPink", DirectionEnum.GAUCHE),
    LREM("Gold", DirectionEnum.CENTRE),
    LR("DodgerBlue", DirectionEnum.DROITE),
    RN("darkblue", DirectionEnum.EX_DROITE),
    JOURNALISTE("darkgreen", DirectionEnum.UNKNOWN),
    UNKNOWN("black", DirectionEnum.UNKNOWN);

    @Getter
    private String color;
    @Getter
    private DirectionEnum direction;
    GroupEnum(String color, DirectionEnum direction) {
        this.color = color;
        this.direction = direction;
    }

    public static GroupEnum findByAbbr(String abbr){
        for(GroupEnum v : values()){
            if( v.name().equals(abbr)){
                return v;
            }
        }
        return null;
    }
}
