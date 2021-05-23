import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class FollowerAnalyzerTest {

  @Test
  public void testGetMarkOfSeveralUsers() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer();
    List<String>     leftUsers        = List.of("T_Bouhafs");
    for (String user : leftUsers) {
      int score = followerAnalyzer.getUserDirection(user);
      System.out.println("score : " + score + " for " + user);
      assertTrue(score <= 0);
    }
  }
}
