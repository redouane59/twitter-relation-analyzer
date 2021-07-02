import lombok.extern.slf4j.Slf4j;
import model.OrientationEnum;
import model.OrientationMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled
public class OrientationMapTest {

  @Test
  public void testToString() {
    OrientationMap orientationMap = new OrientationMap();
    orientationMap.put(OrientationEnum.EX_GAUCHE, 10);
    orientationMap.put(OrientationEnum.GAUCHE, 4);
    orientationMap.put(OrientationEnum.CENTRE, 30);
    orientationMap.put(OrientationEnum.DROITE, 25);
    orientationMap.put(OrientationEnum.EX_DROITE, 48);
    orientationMap.put(OrientationEnum.UNKNOWN, 10);
    System.out.println(orientationMap);
  }
}
