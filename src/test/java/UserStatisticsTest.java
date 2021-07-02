import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.UserList;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import model.UserStatistics;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled
public class UserStatisticsTest {

  TwitterCredentials twitterCredentials1 =
      TwitterClient.OBJECT_MAPPER.readValue(new File("../twitter-credentials - RBA.json"), TwitterCredentials.class);

  public UserStatisticsTest() throws IOException {
  }


  @Test
  public void testToString() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    UserList         users            = followerAnalyzer.getTwitterClient().getLikingUsers("1401283592517455875");
    UserStatistics   userStatistics   = followerAnalyzer.computeUserStatistics(users);
    assertTrue(userStatistics.getFollowersCountAverage() > 0);
    assertTrue(userStatistics.getFollowingsCountAverage() > 0);
    assertTrue(userStatistics.getFollowersCountMedian() > 0);
    assertTrue(userStatistics.getFollowingsCountMedian() > 0);
    assertTrue(userStatistics.getMedianRatio() > 0);
  }
}
