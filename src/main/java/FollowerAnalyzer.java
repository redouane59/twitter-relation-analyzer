import com.github.redouane59.RelationType;
import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.user.User;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.CacheInterceptor;
import model.DataImporter;
import model.DirectionEnum;
import model.GroupEnum;
import model.InfluentUser;
import okhttp3.Cache;
import okhttp3.OkHttpClient.Builder;

@Data
@Slf4j
public class FollowerAnalyzer {

  private       TwitterClient      twitterClient;
  private       List<InfluentUser> influentUsers;
  private final int                FOLLOWING_MARK = 2;
  private final int                FOLLOWER_MARK  = 1;
  private final int                FRIEND_MARK    = 3;

  @SneakyThrows
  public FollowerAnalyzer() {
    TwitterCredentials
        twitterCredentials =
        TwitterClient.OBJECT_MAPPER.readValue(new File("C:/Users/Perso/Documents/GitHub/twitter-credentials - RBA.json"),
                                              TwitterCredentials.class);
    long   cacheSize = 1024L * 1024 * 1024; // 1go
    String path      = "../okhttpCache";
    File   file      = new File(path);

    Builder httpBuilder = new Builder()
        .addNetworkInterceptor(new CacheInterceptor())
        .cache(new Cache(file, cacheSize));
    OkHttpHttpClient okHttpClient = new OkHttpHttpClient(new OkHttpHttpClientConfig(httpBuilder));
    ServiceBuilder builder = new ServiceBuilder(twitterCredentials.getApiKey())
        .httpClient(okHttpClient);

    this.twitterClient = new TwitterClient(twitterCredentials, builder);
    this.influentUsers = DataImporter.importInfluentUser();
  }

  // give a map with each Group Enum, the number of influent users by relation type
  public Map<GroupEnum, Map<RelationType, Integer>> getRelationMap(String userName) {
    Map<GroupEnum, Map<RelationType, Integer>> result = new HashMap<>();
    // init map
    for (GroupEnum group : GroupEnum.values()) {
      result.put(group, new HashMap<>());
    }
    User user = twitterClient.getUserFromUserName(userName);
    for (InfluentUser influentUser : influentUsers) {
      if (influentUser.getId() != null) {
        // limit = 180 calls / 15min
        RelationType relation = twitterClient.getRelationType(user.getId(), influentUser.getId());
        result.get(influentUser.getGroup()).put(relation, result.get(influentUser.getGroup()).getOrDefault(relation, 0) + 1);
      }
    }
    return result;
  }

  // gives a mark for each group enum based on relation
  public Map<DirectionEnum, Integer> getRelationMarkByGroup(Map<GroupEnum, Map<RelationType, Integer>> relationMap) {
    Map<DirectionEnum, Integer> result = new HashMap<>();
    for (GroupEnum group : relationMap.keySet()) {
      DirectionEnum direction = group.getDirection();
      int           mark      = 0;
      for (RelationType relationType : relationMap.get(group).keySet()) {
        if (relationType == RelationType.FOLLOWING) {
          mark += FOLLOWING_MARK;
        } else if (relationType == RelationType.FOLLOWER) {
          mark += FOLLOWER_MARK;
        } else if (relationType == RelationType.FRIENDS) {
          mark += FRIEND_MARK;
        }
      }
      result.put(direction, result.getOrDefault(direction, 0) + mark);
    }
    for (DirectionEnum element : result.keySet()) {
      LOGGER.debug(element + " -> " + result.get(element));
    }
    return result;
  }

  public int getUserDirection(String userName) {
    long start = System.currentTimeMillis();

    LOGGER.debug("*relation map*");
    Map<GroupEnum, Map<RelationType, Integer>> relationMap = getRelationMap(userName);
    LOGGER.debug((System.currentTimeMillis() - start) / 1000F + " s");
    LOGGER.debug("*relation marks*");
    Map<DirectionEnum, Integer> relationMarks = getRelationMarkByGroup(relationMap);
    LOGGER.debug((System.currentTimeMillis() - start) / 1000F + " s");
    LOGGER.debug("*direction*");
    int mark = this.getUserDirection(relationMarks);
    LOGGER.debug((System.currentTimeMillis() - start) / 1000F + " s");
    return mark;
  }

  public int getUserDirection(Map<DirectionEnum, Integer> relationMarks) {
    int result = 0;
    for (DirectionEnum direction : relationMarks.keySet()) {
      result += (direction.getValue() * relationMarks.getOrDefault(direction, 0));
    }
    return result;
  }

}
