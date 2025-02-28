package io.zeebe.bpmn.games;

import io.camunda.zeebe.client.ZeebeClient;
import io.zeebe.bpmn.games.bot.SimpleBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneApplication {

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneApplication.class);

  public static void start() {

    LOG.info("Connect to Zeebe");

    final var zeebeClient =
        ZeebeClient.newClientBuilder().gatewayAddress("localhost:26500").usePlaintext().build();

    LOG.info("Launch workers");

    final var gameListener = new GameStateLogger();
    final var bot = new SimpleBot();

    final var application = new GamesApplication(zeebeClient, gameListener, bot);
    application.start();

    LOG.info("Ready!");
  }
}
