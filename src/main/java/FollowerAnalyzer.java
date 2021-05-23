import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.redouane59.RelationType;
import com.github.redouane59.twitter.TwitterClient;
import com.github.redouane59.twitter.dto.user.User;
import com.github.redouane59.twitter.signature.TwitterCredentials;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient;
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClientConfig;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.Data;
import lombok.SneakyThrows;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Response;

@Data
public class FollowerAnalyzer {

  private static final Logger             LOGGER         = Logger.getLogger(FollowerAnalyzer.class.getName());
  private              TwitterClient      twitterClient;
  private              List<InfluentUser> influentUsers;
  private final        int                FOLLOWING_MARK = 2;
  private final        int                FOLLOWER_MARK  = 1;
  private final        int                FRIEND_MARK    = 3;

  @SneakyThrows
  public FollowerAnalyzer() {
    TwitterCredentials
        twitterCredentials =
        TwitterClient.OBJECT_MAPPER.readValue(new File("C:/Users/Perso/Documents/GitHub/twitter-credentials.json"),
                                              TwitterCredentials.class);
    long   cacheSize = 1024L * 1024 * 1024; // 1go
    String path      = "../okhttpCache";
    File   file      = new File(path);

    Builder httpBuilder = new Builder()
        .addNetworkInterceptor(this.provideCacheInterceptor())
        .cache(new Cache(file, cacheSize));
    OkHttpHttpClient okHttpClient = new OkHttpHttpClient(new OkHttpHttpClientConfig(httpBuilder));
    ServiceBuilder builder = new ServiceBuilder(twitterCredentials.getApiKey())
        .httpClient(okHttpClient);

    this.twitterClient = new TwitterClient(twitterCredentials, builder);

    this.influentUsers = loadInfluentUsers();
  }

  private Interceptor provideCacheInterceptor() {
    return new Interceptor() {
      @Override
      public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        CacheControl cacheControl = new CacheControl.Builder()
            .maxAge(30, TimeUnit.DAYS)
            .build();
        return response.newBuilder()
                       .header("Cache-Control", cacheControl.toString())
                       .build();
      }
    };
  }

  private List<InfluentUser> loadInfluentUsers() {
    String       fileName = "influents_users.json";
    File         file     = new File("src/main/resources/" + fileName);
    ObjectMapper mapper   = new ObjectMapper();
    if (file.exists()) {
      try {
        return List.of(mapper.readValue(file, InfluentUser[].class));
      } catch (Exception e) {
        LOGGER.severe(" KO!! " + e.getMessage());
      }
    }
    return null;
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
  public Map<GroupEnum, Integer> getRelationMarkByGroup(Map<GroupEnum, Map<RelationType, Integer>> relationMap) {
    Map<GroupEnum, Integer> result = new HashMap<>();
    for (GroupEnum group : relationMap.keySet()) {
      int mark = 0;
      for (RelationType relationType : relationMap.get(group).keySet()) {
        if (relationType == RelationType.FOLLOWING) {
          mark += FOLLOWING_MARK;
        } else if (relationType == RelationType.FOLLOWER) {
          mark += FOLLOWER_MARK;
        } else if (relationType == RelationType.FRIENDS) {
          mark += FRIEND_MARK;
        }
      }
      result.put(group, mark);
    }
    return result;
  }

  public int getUserDirection(String userName) {
    long start = System.currentTimeMillis();

    System.out.println("*relation map*");
    Map<GroupEnum, Map<RelationType, Integer>> relationMap = getRelationMap(userName);
    System.out.println((System.currentTimeMillis() - start) / 1000F + " s");
    System.out.println("*relation marks*");
    Map<GroupEnum, Integer> relationMarks = getRelationMarkByGroup(relationMap);
    System.out.println((System.currentTimeMillis() - start) / 1000F + " s");
    System.out.println("*direction*");
    int mark = this.getUserDirection(relationMarks);
    System.out.println((System.currentTimeMillis() - start) / 1000F + " s");
    return mark;
  }

  public int getUserDirection(Map<GroupEnum, Integer> relationMarks) {
    int result = 0;
    for (GroupEnum groupEnum : relationMarks.keySet()) {
      result += (groupEnum.getDirection().getValue() * relationMarks.getOrDefault(groupEnum, 0));
    }
    return result;
  }

}
