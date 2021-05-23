package model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataImporter {

  public static List<InfluentUser> importInfluentUser() {
    String       fileName = "influents_users.json";
    File         file     = new File("src/main/resources/" + fileName);
    ObjectMapper mapper   = new ObjectMapper();
    if (file.exists()) {
      try {
        return List.of(mapper.readValue(file, InfluentUser[].class));
      } catch (Exception e) {
        LOGGER.error(" user importation KO ! " + e.getMessage());
      }
    } else {
      LOGGER.error("file not foun");
    }
    return null;
  }
}
