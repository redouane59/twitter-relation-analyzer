import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class FollowerAnalyzerTest {

  @Test
  public void testGetMarkOfSeveralUsers() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer();
    List<String>     leftUsers        = List.of("mouadibun");
    for (String user : leftUsers) {
      int score = followerAnalyzer.getUserDirection(user);
      LOGGER.debug("score : " + score + " for " + user);
    }
  }
}
