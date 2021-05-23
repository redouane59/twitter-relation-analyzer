import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.redouane59.twitter.dto.user.UserV2;
import java.io.IOException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonDeserialize(using = InfluentUser.InfluentUserDeserializer.class)

public class InfluentUser extends UserV2 {

  private GroupEnum     group;
  private DirectionEnum direction;

  public InfluentUser(String name, String id, GroupEnum group, DirectionEnum direction) {
    this.group     = group;
    this.direction = direction;
    this.setData(UserData.builder().id(id).name(name).build());
  }

  public static class InfluentUserDeserializer extends StdDeserializer<InfluentUser> {

    public InfluentUserDeserializer() {
      this(null);
    }

    public InfluentUserDeserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public InfluentUser deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
      JsonNode  node  = jp.getCodec().readTree(jp);
      String    name  = node.get("name").asText();
      String    id    = node.get("id").asText();
      GroupEnum group = GroupEnum.UNKNOWN;
      if (node.has("group")) {
        group = GroupEnum.findByAbbr(node.get("group").asText());
      }
      DirectionEnum direction = DirectionEnum.UNKNOWN;
      if (node.has("direction")) {
        DirectionEnum.findByAbbr(node.get("direction").asText());
      }
      return new InfluentUser(name, id, group, direction);
    }
  }

}


