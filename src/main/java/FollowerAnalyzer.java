import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig;
import io.github.redouane59.RelationType;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.dto.user.UserList;
import io.github.redouane59.twitter.dto.user.UserV2.UserData;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.CacheInterceptor;
import model.DataImporter;
import model.GroupEnum;
import model.InfluentUser;
import model.OrientationEnum;
import model.OrientationMap;
import model.UserStatistics;
import okhttp3.Cache;
import okhttp3.OkHttpClient.Builder;

@Getter
@Setter
@Slf4j
public class FollowerAnalyzer {

  private       TwitterClient      twitterClient;
  private       List<InfluentUser> influentUsers; // from json
  private final int                FOLLOWING_MARK = 2;
  private final int                FOLLOWER_MARK  = 1; // unused
  private final int                FRIEND_MARK    = 3; // unused
  private       long               exGaucheCount;
  private       long               gaucheCount;
  private       long               centreCount;
  private       long               droiteCount;
  private       long               exDroiteCount;
  private       UserStatistics     userStatistics;

  @SneakyThrows
  public FollowerAnalyzer(TwitterCredentials twitterCredentials) {
    this.twitterClient = new TwitterClient(twitterCredentials, this.getServiceBuilder(twitterCredentials.getApiKey()));
    this.influentUsers = DataImporter.importInfluentUser("influents_users.json", this.twitterClient);
    for (InfluentUser infuencer : influentUsers) {
      this.isFollowersIdsDataReliable(infuencer);
    }
    this.exGaucheCount = this.countInfluencerByOrientation(OrientationEnum.EX_GAUCHE);
    this.gaucheCount   = this.countInfluencerByOrientation(OrientationEnum.GAUCHE);
    this.centreCount   = this.countInfluencerByOrientation(OrientationEnum.CENTRE);
    this.droiteCount   = this.countInfluencerByOrientation(OrientationEnum.DROITE);
    this.exDroiteCount = this.countInfluencerByOrientation(OrientationEnum.EX_DROITE);
  }

  public double getUserOrientationFromName(String userName) {
    Map<GroupEnum, Map<RelationType, Integer>> relationMap   = getRelationMap(twitterClient.getUserFromUserName(userName).getId());
    OrientationMap                             relationMarks = getRelationMarkByGroup(relationMap);
    double                                     mark          = this.getUserOrientation(relationMarks);
    return mark;
  }

  public double getUserOrientation(String userId) {
    Map<GroupEnum, Map<RelationType, Integer>> relationMap   = getRelationMap(userId);
    OrientationMap                             relationMarks = getRelationMarkByGroup(relationMap);
    double                                     mark          = this.getUserOrientation(relationMarks);
    return mark;
  }


  // give a map with each Group Enum, the number of influent users by relation type
  private Map<GroupEnum, Map<RelationType, Integer>> getRelationMap(String userId) {
    Map<GroupEnum, Map<RelationType, Integer>> result = new HashMap<>();
    // init map
    for (GroupEnum group : GroupEnum.values()) {
      result.put(group, new HashMap<>());
    }
    for (InfluentUser influentUser : influentUsers) {
      if (influentUser.getId() != null) {
        RelationType relation = influentUser.getPartialRelationType(userId);
        result.get(influentUser.getGroup()).put(relation, result.get(influentUser.getGroup()).getOrDefault(relation, 0) + 1);
      }
    }
    return result;
  }

  // gives a mark for each orientation based on relation
  public long countInfluencerByOrientation(OrientationEnum orientation) {
    return this.influentUsers.stream().filter(iu -> iu.getOrientation() == orientation).count();
  }

  public OrientationMap getRelationMarkByGroup(Map<GroupEnum, Map<RelationType, Integer>> relationMap) {
    OrientationMap result = new OrientationMap();
    for (GroupEnum group : relationMap.keySet()) {
      OrientationEnum orientation = group.getOrientation();
      int             mark        = 0;
      for (RelationType relationType : relationMap.get(group).keySet()) {
        if (relationType == RelationType.FOLLOWING) {
          mark += FOLLOWING_MARK;
        } else if (relationType == RelationType.FOLLOWER) {
          mark += FOLLOWER_MARK;
        } else if (relationType == RelationType.FRIENDS) {
          mark += FRIEND_MARK;
        }
      }
      result.put(orientation, result.getOrDefault(orientation, 0) + mark);
    }
    return result;
  }

  public OrientationMap getRelationMarkByGroup(String tweetId) {
    UserList response = this.twitterClient.getLikingUsers(tweetId);
    this.userStatistics = this.computeUserStatistics(response);
    OrientationMap orientations = new OrientationMap();
    if (response.getData() == null) {
      LOGGER.error("data null");
      return new OrientationMap();
    }
    for (User user : response.getData()) {
      OrientationEnum userOrientation = this.getUserOrientationSimple(user.getId());
      orientations.put(userOrientation, orientations.getOrDefault(userOrientation, 0) + 1);
    }
    return orientations;
  }

  public UserStatistics computeUserStatistics(UserList UserList) {
    List<UserData> users = UserList.getData();
    double
        followersCountAvg =
        UserList.getData().stream().map(UserData::getFollowersCount).mapToInt(Integer::intValue).average().getAsDouble();
    double
        followingsCountAvg =
        UserList.getData().stream().map(UserData::getFollowingCount).mapToInt(Integer::intValue).average().getAsDouble();

    DoubleStream sortedFollowersCounts = users.stream().mapToDouble(UserData::getFollowersCount).sorted();
    double followersCountMedian = users.size() % 2 == 0 ?
                                  sortedFollowersCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                                  sortedFollowersCounts.skip(users.size() / 2).findFirst().getAsDouble();

    DoubleStream sortedFollowingsCounts = users.stream().mapToDouble(UserData::getFollowingCount).sorted();
    double followingsCountMedian = users.size() % 2 == 0 ?
                                   sortedFollowingsCounts.skip(users.size() / 2 - 1).limit(2).average().getAsDouble() :
                                   sortedFollowingsCounts.skip(users.size() / 2).findFirst().getAsDouble();
    return UserStatistics.builder()
                         .followersCountAverage(followersCountAvg)
                         .followingsCountAverage(followingsCountAvg)
                         .followersCountMedian((int) followersCountMedian)
                         .followingsCountMedian((int) followingsCountMedian)
                         .build();
  }

  public OrientationEnum getUserOrientationSimple(String userId) {
    Map<OrientationEnum, Double> orientationMap = new HashMap<>();
    for (InfluentUser user : this.influentUsers) {
      double value = 0;
      if (user.isFollowedByUser(userId)) {
        switch (user.getOrientation()) {
          case EX_GAUCHE -> value = (double) 1 / exGaucheCount;
          case GAUCHE -> value = (double) 1 / gaucheCount;
          case CENTRE -> value = (double) 1 / centreCount;
          case DROITE -> value = (double) 1 / droiteCount;
          case EX_DROITE -> value = (double) 1 / exDroiteCount;
        }
      }
      orientationMap.put(user.getOrientation(), orientationMap.getOrDefault(user.getOrientation(), (double) 0) + value);
    }
    return this.getOrientationFromMap(orientationMap);
  }

  public OrientationEnum getOrientationFromMap(Map<OrientationEnum, Double> map) {
    OrientationEnum result   = OrientationEnum.UNKNOWN;
    double          maxValue = 0.05;
    for (Map.Entry<OrientationEnum, Double> entry : map.entrySet()) {
      if (entry.getValue() > maxValue) {
        result   = entry.getKey();
        maxValue = entry.getValue();
      }
    }
    return result;
  }


  private double getUserOrientation(OrientationMap relationMarks) {
    double result        = 0;
    long   exGaucheCount = this.countInfluencerByOrientation(OrientationEnum.EX_GAUCHE);
    long   gaucheCount   = this.countInfluencerByOrientation(OrientationEnum.GAUCHE);
    long   centreCount   = this.countInfluencerByOrientation(OrientationEnum.CENTRE);
    long   droiteCount   = this.countInfluencerByOrientation(OrientationEnum.DROITE);
    long   exDroiteCount = this.countInfluencerByOrientation(OrientationEnum.EX_DROITE);

    for (OrientationEnum orientation : relationMarks.keySet()) {
      double value = orientation.getValue();
      switch (orientation) {
        case EX_GAUCHE -> value /= exGaucheCount;
        case GAUCHE -> value /= gaucheCount;
        case CENTRE -> value /= centreCount;
        case DROITE -> value /= droiteCount;
        case EX_DROITE -> value /= exDroiteCount;
      }
      result += (value * relationMarks.getOrDefault(orientation, 0));
    }
    return result;
  }


  public boolean isFollowersIdsDataReliable(InfluentUser influencer) {
    int offlineFollowersCount = influencer.getFollowerIds().size();
    if (offlineFollowersCount == 0) {
      LOGGER.error("No offline followers found for " + influencer.getName());
      return false;
    }
    int    apiFollowersCount = twitterClient.getUserFromUserName(influencer.getName()).getFollowersCount();
    int    diff              = Math.abs(apiFollowersCount - offlineFollowersCount);
    double acceptableDelta   = 0.1;
    if (diff > acceptableDelta * apiFollowersCount) {
      LOGGER.error("/!\\ Follower ids data doesn't look accurate for "
                   + influencer.getName()
                   + " ("
                   + offlineFollowersCount
                   + " ids VS "
                   + apiFollowersCount
                   + " followers ");
      return false;
    } else {
      LOGGER.debug("followers ids OK for " + influencer.getName());
      return true;
    }
  }

  public boolean saveFollowersIdsIfDataNotReliable(InfluentUser user) {
    if (!this.isFollowersIdsDataReliable(user)) {
      List<String> followers = this.getTwitterClient().getFollowersIds(user.getId());
      user.setFollowerIds(new HashSet<>(followers));
      File file = new File("src/main/resources/users/" + user.getName() + ".json");
      try {
        TwitterClient.OBJECT_MAPPER.writeValue(file, followers);
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
      }
      return true;
    }
    return false;
  }

  private ServiceBuilder getServiceBuilder(String apiKey) {
    long   cacheSize = 1024L * 1024 * 1024 * 8; // 8go
    String path      = "../okhttpCache";
    File   file      = new File(path);
    Builder httpBuilder = new Builder()
        .addNetworkInterceptor(new CacheInterceptor())
        .cache(new Cache(file, cacheSize));
    OkHttpHttpClient okHttpClient = new OkHttpHttpClient(new OkHttpHttpClientConfig(httpBuilder));
    return new ServiceBuilder(apiKey)
        .httpClient(okHttpClient);
  }

}
