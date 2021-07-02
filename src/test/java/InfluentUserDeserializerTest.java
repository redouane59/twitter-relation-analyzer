import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import model.InfluentUser;
import model.OrientationEnum;
import org.junit.jupiter.api.Test;

@Slf4j
public class InfluentUserDeserializerTest {

  TwitterCredentials twitterCredentials =
      TwitterClient.OBJECT_MAPPER.readValue(new File("../twitter-credentials - RBA.json"), TwitterCredentials.class);

  public InfluentUserDeserializerTest() throws IOException {
  }


  @Test
  public void testDeserializeUsers() throws IOException {
    String       fileName = "influents_users.json";
    File         file     = new File("src/main/resources/" + fileName);
    ObjectMapper mapper   = new ObjectMapper();

    List<InfluentUser> users = List.of(mapper.readValue(file, InfluentUser[].class));

    assertNotNull(users);
    assertNotNull(users.get(0).getId());
    assertNotNull(users.get(0).getGroup());
    assertNotNull(users.get(0).getOrientation());
  }

  @Test
  public void testCountOrientation() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials);
    assertTrue(followerAnalyzer.countInfluencerByOrientation(OrientationEnum.EX_GAUCHE) > 0);
    assertTrue(followerAnalyzer.countInfluencerByOrientation(OrientationEnum.GAUCHE) > 0);
    assertTrue(followerAnalyzer.countInfluencerByOrientation(OrientationEnum.CENTRE) > 0);
    assertTrue(followerAnalyzer.countInfluencerByOrientation(OrientationEnum.DROITE) > 0);
    assertTrue(followerAnalyzer.countInfluencerByOrientation(OrientationEnum.EX_DROITE) > 0);
  }

  @Test
  public void testLog() {
    LOGGER.trace("trace");
    LOGGER.debug("debug");
    LOGGER.info("info");
    LOGGER.warn("warn");
    LOGGER.error("error");
  }
}
