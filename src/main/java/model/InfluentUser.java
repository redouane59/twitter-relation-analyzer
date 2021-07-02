package model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.github.redouane59.RelationType;
import io.github.redouane59.twitter.dto.user.UserV2;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@NoArgsConstructor
@JsonDeserialize(using = InfluentUser.InfluentUserDeserializer.class)
@JsonSerialize(using = InfluentUser.InfluentUserSerializer.class)
@Slf4j
public class InfluentUser extends UserV2 {

  private              GroupEnum       group;
  private              OrientationEnum orientation;
  private              HashSet<String> followerIds   = new HashSet<>();
  private final static ObjectMapper    OBJECT_MAPPER = new ObjectMapper();

  public InfluentUser(String name, String id, GroupEnum group, OrientationEnum orientation) {
    this.group       = group;
    this.orientation = orientation;
    this.setData(UserData.builder().id(id).name(name).build());
    this.loadFollowerIds();
  }

  private void loadFollowerIds() {
    File file = new File("src/main/resources/users/" + this.getName() + ".json");
    if (file.exists()) {
      try {
        this.followerIds = new HashSet<>(List.of(OBJECT_MAPPER.readValue(file, String[].class)));
      } catch (IOException ioException) {
        LOGGER.error(ioException.getMessage());
      }
    } else {
      LOGGER.debug("No file found for " + this.getName());
    }
  }

  public boolean isFollowedByUser(String userId) {
    return this.followerIds.contains(userId);
  }

  public RelationType getPartialRelationType(String userId) {
    if (this.isFollowedByUser(userId)) {
      return RelationType.FOLLOWING;
    } else {
      return RelationType.NONE;
    }
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
      } else {
        LOGGER.info(name + " group KO");
      }
      OrientationEnum orientation = OrientationEnum.UNKNOWN;
      if (node.has("orientation")) {
        orientation = OrientationEnum.findByAbbr(node.get("orientation").asText());
      } else {
        LOGGER.info(name + " orientation KO");
      }
      return new InfluentUser(name, id, group, orientation);
    }
  }

  public static class InfluentUserSerializer extends StdSerializer<InfluentUser> {

    public InfluentUserSerializer() {
      this(null);
    }

    public InfluentUserSerializer(Class<InfluentUser> t) {
      super(t);
    }

    @Override
    public void serialize(
        InfluentUser user, JsonGenerator jgen, SerializerProvider provider)
    throws IOException {

      jgen.writeStartObject();
      jgen.writeStringField("name", user.getName());
      jgen.writeStringField("id", user.getId());
      jgen.writeStringField("group", user.getGroup().name());
      jgen.writeStringField("orientation", user.getOrientation().name());
      jgen.writeEndObject();
    }
  }


}


