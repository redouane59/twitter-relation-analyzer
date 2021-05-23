import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

public class InfluentUserDeserializerTest {

  @Test
  public void testDeserializeUsers() throws IOException {
    String       fileName = "influents_users.json";
    File         file     = new File("src/main/resources/" + fileName);
    ObjectMapper mapper   = new ObjectMapper();

    List<InfluentUser> users = List.of(mapper.readValue(file, InfluentUser[].class));

    assertNotNull(users);
    assertNotNull(users.get(0).getId());
    assertNotNull(users.get(0).getGroup());
    assertNotNull(users.get(0).getDirection());
  }
}
