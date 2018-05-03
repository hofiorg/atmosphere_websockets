package org.hofi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.wasync.*;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Client {
  private final static Logger logger = LoggerFactory.getLogger(Client.class);
  private final static ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) throws IOException {
    new Client().connect();
  }

  private void connect() throws IOException {
    AtmosphereClient client = ClientFactory.getDefault().newClient(AtmosphereClient.class);
    RequestBuilder request = client.newRequestBuilder()
      .method(Request.METHOD.GET)
      .uri("http://10.2.2.14:5120/endpoint")
      .trackMessageLength(true)
      .encoder(new Encoder<Message, String>() {
        @Override
        public String encode(Message data) {
          try {
            return mapper.writeValueAsString(data);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      })
      .decoder(new Decoder<String, Message>() {
        @Override
        public Message decode(Event type, String data) {

          data = data.trim();

          // Padding from Atmosphere, skip
          if (data.length() == 0) {
            return null;
          }

          if (type.equals(Event.MESSAGE)) {
            try {
              return mapper.readValue(data, Message.class);
            } catch (IOException e) {
              logger.debug("Invalid message {}", data);
              return null;
            }
          } else {
            return null;
          }
        }
      })
      .transport(Request.TRANSPORT.WEBSOCKET)
      .transport(Request.TRANSPORT.SSE)
      .transport(Request.TRANSPORT.LONG_POLLING);

    logger.info("Socket creation");
    Socket socket = client.create();
    socket.on(Event.MESSAGE, new Function<String>() {
      @Override
      public void on(String t) {
        logger.info("String {}: {}", t);
      }
    }).on(Event.MESSAGE, new Function<Message>() {
      @Override
      public void on(Message t) {
        logger.info("Message {}: {}", t.getText());
      }
    }).on(new Function<Throwable>() {

      @Override
      public void on(Throwable t) {
        t.printStackTrace();
      }

    }).on(Event.CLOSE.name(), new Function<String>() {
      @Override
      public void on(String t) {
        logger.info("Connection closed");
      }
    }).open(request.build());
  }
}
