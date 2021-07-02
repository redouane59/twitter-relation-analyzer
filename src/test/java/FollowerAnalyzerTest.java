import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.Tweet;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.tweet.TweetType;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.DataImporter;
import model.InfluentUser;
import model.OrientationEnum;
import model.OrientationMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled
public class FollowerAnalyzerTest {

  TwitterCredentials twitterCredentials1 =
      TwitterClient.OBJECT_MAPPER.readValue(new File("../twitter-credentials - RBA.json"), TwitterCredentials.class);

  TwitterCredentials twitterCredentials2 =
      TwitterClient.OBJECT_MAPPER.readValue(new File("../twitter-credentials - RBA.json"), TwitterCredentials.class);

  private static DecimalFormat df2 = new DecimalFormat("#.##");


  public FollowerAnalyzerTest() throws IOException {
    twitterCredentials2.setAccessToken(null);
    twitterCredentials2.setAccessTokenSecret(null);
  }


  @Test
  public void testGetMarkOfSeveralUsers() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    List<String>     leftUsers        = List.of("magicalorrs");
    for (String user : leftUsers) {
      double score = followerAnalyzer.getUserOrientationFromName(user);
      LOGGER.info("score : " + score + " for " + user);
    }
  }

  @SneakyThrows
  @Test
  public void fillMissingUsers() {
    FollowerAnalyzer   followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    List<InfluentUser> users            = DataImporter.importInfluentUser("test_users.json", followerAnalyzer.getTwitterClient());
    for (InfluentUser user : users) {
      if (!followerAnalyzer.isFollowersIdsDataReliable(user)) {
        followerAnalyzer.saveFollowersIdsIfDataNotReliable(user);
        OrientationMap globalProfile = new OrientationMap();
        globalProfile.put(OrientationEnum.EX_GAUCHE, 0);
        globalProfile.put(OrientationEnum.GAUCHE, 0);
        globalProfile.put(OrientationEnum.CENTRE, 0);
        globalProfile.put(OrientationEnum.DROITE, 0);
        globalProfile.put(OrientationEnum.EX_DROITE, 0);
        for (String followerId : user.getFollowerIds()) {
          OrientationEnum followerProfil = followerAnalyzer.getUserOrientationSimple(followerId);
          globalProfile.put(followerProfil, globalProfile.getOrDefault(followerProfil, 0) + 1);
        }
        user.setOrientation(globalProfile.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey());

        String json = TwitterClient.OBJECT_MAPPER.writeValueAsString(user);
        LOGGER.info(json);

      }
    }
    System.out.println("end");
  }

  @Test
  public void testGetUserOrientation() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    User             user             = followerAnalyzer.getTwitterClient().getUserFromUserName("RedTheOne");
    OrientationEnum  orientation      = followerAnalyzer.getUserOrientationSimple(user.getId());
    System.out.println(orientation);
  }

  @Test
  public void testGetPublicationOrientationByUser() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    String           userId           = "47336512";
    TweetList lastUserTweets = followerAnalyzer.getTwitterClient().getUserTimeline(userId,
                                                                                   AdditionalParameters.builder()
                                                                                                       .maxResults(20).build());
    for (Tweet tweet : lastUserTweets.getData()) {
      if (tweet.getTweetType() != TweetType.RETWEETED) {
        System.out.println("tweet #" + tweet.getId());
        OrientationMap result = followerAnalyzer.getRelationMarkByGroup(tweet.getId());
        System.out.println(result);
        System.out.println("");
      }
    }
  }


  @Test
  public void testGetPublicationOrientation() {
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);
    String           tweetId          = "1405833482857746437";
    Tweet            tweet            = followerAnalyzer.getTwitterClient().getTweet(tweetId);
    OrientationMap   result           = followerAnalyzer.getRelationMarkByGroup(tweetId);
    System.out.println("\nAnalyzing tweet " + tweetId + " of @" + tweet.getUser().getName() + "...");
    System.out.println(result);
    System.out.println("Nombre de followers median -> " + followerAnalyzer.getUserStatistics().getFollowersCountMedian());
    System.out.println("Nombre de followings median -> " + followerAnalyzer.getUserStatistics().getFollowingsCountMedian());
    System.out.println("Ratio median -> " + df2.format(followerAnalyzer.getUserStatistics().getMedianRatio()));
  }

  @Test
  public void loadMissingFollowers() {

    ObjectMapper     mapper           = new ObjectMapper();
    FollowerAnalyzer followerAnalyzer = new FollowerAnalyzer(twitterCredentials1);

    System.out.println("\n*** STARTING LOADING FOLLOWERS ***\n");
    for (int i = 0; i < followerAnalyzer.getInfluentUsers().size(); i++) {
      try {
        // 1
        InfluentUser user = followerAnalyzer.getInfluentUsers().get(i);
        System.out.println("anayzing user : " + user.getName());
        if (!followerAnalyzer.isFollowersIdsDataReliable(user)) {
          List<String> followers = followerAnalyzer.getTwitterClient().getFollowersIds(user.getId());
          assertTrue(followers.size() > 0);
          File file = new File("src/main/resources/users/" + user.getName() + ".json");
          mapper.writeValue(file, followers);
        }
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
      }
    }
  }

  @Test
  public void loadMissingFollowers2() {

    ObjectMapper mapper = new ObjectMapper();
    twitterCredentials2.setAccessToken(null);
    twitterCredentials2.setAccessTokenSecret(null);
    FollowerAnalyzer followerAnalyzer2 = new FollowerAnalyzer(twitterCredentials2);

    List<String> userNames = List.of("RokhayaDiallo");
    System.out.println("\n*** STARTING LOADING FOLLOWERS ***\n");

    for (String userName : userNames) {
      try {
        // 2
        User user2 = followerAnalyzer2.getTwitterClient().getUserFromUserName(userName);
        System.out.println("anayzing user : " + user2.getName());
        List<String> followers2 = followerAnalyzer2.getTwitterClient().getFollowersIds(user2.getId());
        assertTrue(followers2.size() > 0);
        File file2 = new File("src/main/resources/users/" + user2.getName() + ".json");
        mapper.writeValue(file2, followers2);
        assertTrue(file2.exists());
      } catch (Exception e) {
        LOGGER.error(e.getMessage());
      }
    }
  }
}
