package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackSession {

  private static final Logger LOG = LoggerFactory.getLogger(SlackSession.class);

  private final Map<String, String> channelIdByUserId = new HashMap<>();
  private final Map<Long, GameInfo> games = new HashMap<>();

  @Autowired private MethodsClient methodsClient;

  public String getChannelId(String userId) {
    return channelIdByUserId.computeIfAbsent(userId, this::resolveChannelId);
  }

  private String resolveChannelId(String userId) {
    try {
      final var response = methodsClient.imOpen(r -> r.user(userId));

      return response.getChannel().getId();

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getUserIdsOfGame(long key) {
    return Optional.ofNullable(games.get(key))
        .map(GameInfo::getUserIds)
        .orElseThrow(() -> new RuntimeException("no game found for key: " + key));
  }

  public void putGame(long key, List<String> userIds) {
    games.put(key, new GameInfo(userIds));
  }

  public void removeGame(long key) {
    games.remove(key);
  }

  public static class GameInfo {

    private final List<String> userIds;

    public GameInfo(List<String> userIds) {
      this.userIds = userIds;
    }

    public List<String> getUserIds() {
      return userIds;
    }
  }
}
