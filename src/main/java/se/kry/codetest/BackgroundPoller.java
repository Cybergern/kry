package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BackgroundPoller extends AbstractVerticle {

  private WebClient client;
  private DBConnector connector;

  BackgroundPoller(WebClient client, DBConnector connector) {
    this.client = client;
    this.connector = connector;
  }

  public Future<List<Service>> pollServices(DBConnector connector) {
    return connector.getAllServices().setHandler(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        ar.result().forEach(this::pollService);
      }
    });
  }

  public Future<String> pollService(Service service) {
    System.out.println("Polling service " + service.getName());
    client.get(80, service.getUrl(), "/").send(ar -> {
      if (ar.failed()) {
        connector.setServiceStatus(service.getName(), "DOWN");
      } else {
        connector.setServiceStatus(service.getName(), this.translateStatusCode(ar.result().statusCode()));
      }
    });
    return Future.succeededFuture("Polling complete");
  }

  private String translateStatusCode(int code) {
    if (code == 200) {
      return "OK";
    } else {
      return "DOWN";
    }
  }
}
