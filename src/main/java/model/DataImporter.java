package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataImporter {

  public static List<InfluentUser> importInfluentUser(String userName, TwitterClient twitterClient) {
    File         file   = new File("src/main/resources/" + userName);
    ObjectMapper mapper = new ObjectMapper();
    if (file.exists()) {
      try {
        List<InfluentUser> influentUsers = List.of(mapper.readValue(file, InfluentUser[].class));
        return fixData(influentUsers, twitterClient);
      } catch (Exception e) {
        LOGGER.error(" user importation KO ! " + e.getMessage());
      }
    } else {
      LOGGER.error("file not foun");
    }
    return null;
  }

  private static List<InfluentUser> fixData(List<InfluentUser> influentUsers, TwitterClient twitterClient) {
    for (InfluentUser influentUser : influentUsers) {
      if (influentUser.getId().isEmpty()) {
        User user = twitterClient.getUserFromUserName(influentUser.getName());
        influentUser.getData().setId(user.getId());
      }
    }
    return influentUsers;
  }
}
