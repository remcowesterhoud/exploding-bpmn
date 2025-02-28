package io.zeebe.bpmn.games.slack;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SlackUtil {

  private static final Map<CardType, String> cardEmojis1 =
      Map.of(
          CardType.EXPLODING, ":bpmn-error-throw-event-red:",
          CardType.DEFUSE, ":bpmn-error-catch-event-green:",
          CardType.ATTACK, ":bpmn-parallel-gateway-purple:",
          CardType.SEE_THE_FUTURE, ":bpmn-timer-catch-event-blue:",
          CardType.ALTER_THE_FUTURE, ":bpmn-compensation-throw-event-blue:",
          CardType.DRAW_FROM_BOTTOM, ":bpmn-escalation-throw-event-blue:",
          CardType.SHUFFLE, ":bpmn-complex-gateway-blue:",
          CardType.SKIP, ":bpmn-link-throw-event-blue:",
          CardType.NOPE, ":bpmn-cancel-throw-event-yellow:",
          CardType.FAVOR, ":bpmn-receive-task:");

  private static final Map<CardType, String> cardEmojis2 =
      Map.of(
          CardType.FERAL_CAT,
          ":bpmn-none-task:",
          CardType.CAT_1,
          ":bpmn-user-task:",
          CardType.CAT_2,
          ":bpmn-manual-task:",
          CardType.CAT_3,
          ":bpmn-service-task:",
          CardType.CAT_4,
          ":bpmn-script-task:",
          CardType.CAT_5,
          ":bpmn-business-rule-task:");

  private static final Map<CardType, String> cardEmojis;

  static {
    cardEmojis = new HashMap<>();
    cardEmojis.putAll(cardEmojis1);
    cardEmojis.putAll(cardEmojis2);
  }
  ;

  public static String formatCard(Card card) {
    final var type = card.getType();
    return formatCardType(type);
  }

  public static String formatButtonCards(List<Card> cards) {

    final String emojis =
        cards.stream()
            .map(
                card -> {
                  final var type = card.getType();
                  return cardEmojis.get(type);
                })
            .collect(Collectors.joining(" & "));

    final String names =
        cards.stream()
            .map(
                card -> {
                  final var type = card.getType();
                  return type.name().toLowerCase().replaceAll("_", " ");
                })
            .collect(Collectors.joining(", ", "(", ")"));

    return emojis + " " + names;
  }

  public static String formatCardType(CardType type) {
    return Optional.ofNullable(cardEmojis.get(type))
        .map(
            e -> {
              final var typeName = type.name().toLowerCase().replaceAll("_", " ");
              return e + String.format(" _(%s)_", typeName);
            })
        .orElse(String.format("*%s*", type));
  }

  public static String formatCardTypePlain(CardType type) {
    return Optional.ofNullable(cardEmojis.get(type))
        .map(
            e -> {
              final var typeName = type.name().toLowerCase().replaceAll("_", " ");
              return e + String.format(" (%s)", typeName);
            })
        .orElse(String.format("%s", type));
  }

  public static String formatCards(List<Card> cards) {
    return cards.stream().map(SlackUtil::formatCard).collect(Collectors.joining(", "));
  }

  public static String formatPlayer(String userId) {
    if (!isBot(userId)) {
      return String.format("<@%s>", userId);
    } else {
      return String.format(":robot_face: _%s_", userId);
    }
  }

  public static boolean isBot(String userId) {
    return userId.startsWith("bot_");
  }
}
