package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectAction implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public SelectAction(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "selectAction")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var player = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(player);

    final var deck = variables.getDeck();

    final var playerNames = variables.getPlayerNames();
    final var nextPlayerIndex = variables.getNextPlayerIndex();
    final var nextPlayer = playerNames.get(nextPlayerIndex);

    interaction
        .selectCardsToPlay(player, hand, deck.size(), nextPlayer)
        .thenAccept(cardsToPlay -> completeJob(jobClient, job, variables, player, cardsToPlay));
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      String player,
      List<Card> cardsToPlay) {

    variables.putCards(cardsToPlay).putLastPlayedCards(cardsToPlay);

    if (cardsToPlay.isEmpty()) {
      variables.putAction("pass");
      listener.playerPassed(GameContext.of(job), player);

    } else {

      final var card = cardsToPlay.get(0).getType();
      final var action = card.isCatCard() ? "cat-pair" : card.name().toLowerCase();

      variables.putAction(action);
      listener.cardsPlayed(GameContext.of(job), player, cardsToPlay);
    }

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
