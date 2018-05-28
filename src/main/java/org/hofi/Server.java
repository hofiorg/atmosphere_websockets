package org.hofi;

import org.atmosphere.config.service.Disconnect;
import org.atmosphere.config.service.ManagedService;
import org.atmosphere.config.service.Ready;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedService(path = "/endpoint")
public class Server {
  private final Logger logger = LoggerFactory.getLogger(Server.class);

  @Ready
  public Message onReady(final AtmosphereResource r) {
    String uuid = r.uuid();
    logger.debug("Browser {} connected. " + uuid);

    return new Message("hello world");
  }

  @Disconnect
  public void onDisconnect(AtmosphereResourceEvent event) {
    if (event.isCancelled()) {
      logger.debug("Browser {} unexpectedly disconnected " +  event.getResource().uuid());
    } else if (event.isClosedByClient()) {
      logger.debug("Browser {} closed the connection " + event.getResource().uuid());
    }
  }

  @org.atmosphere.config.service.Message(encoders = {JacksonEncoder.class}, decoders = {JacksonDecoder.class})
  public Message onMessage(Message message) {
    logger.debug("{} just send {} " + message.getText());
    return message;
  }
}