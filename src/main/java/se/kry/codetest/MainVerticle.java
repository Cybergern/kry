package se.kry.codetest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;
import java.util.stream.Collectors;

public class MainVerticle extends AbstractVerticle {

  private DBConnector connector;
  private BackgroundPoller poller;
  private String databasePath;

  MainVerticle(String databasePath) {
    this.databasePath = databasePath;
  }

  @Override
  public void start(Future<Void> startFuture) {
    connector = new DBConnector(vertx, this.databasePath);
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    poller = new BackgroundPoller(WebClient.create(vertx), connector);
    vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(connector));
    setRoutes(router);
    vertx
        .createHttpServer()
        .requestHandler(router)
        .listen(8080, result -> {
          if (result.succeeded()) {
            System.out.println("KRY code test service started");
            startFuture.complete();
          } else {
            startFuture.fail(result.cause());
          }
        });
  }

  @Override
  public void stop() throws Exception {
    connector.close();
  }

  private void setRoutes(Router router){
    router.route("/*").handler(StaticHandler.create());
    router.get("/service").handler(this::getServicesHandler);
    router.post("/service").handler(this::saveServicesHandler);
    router.delete("/service").handler(this::deleteServicesHandler);
  }

  private void getServicesHandler(RoutingContext req) {
    connector.getAllServices().setHandler(services -> {
      if (services.failed()) {
        req.response().setStatusCode(500).setStatusMessage("Unable to fetch services");
      } else {
        List<JsonObject> jsonServices = services.result().stream()
                .map(service ->
                        new JsonObject()
                                .put("name", service.getName())
                                .put("status", service.getStatus()))
                .collect(Collectors.toList());
        req.response()
                .putHeader("content-type", "application/json")
                .end(new JsonArray(jsonServices).encode());
      }
    });
  }

  private void saveServicesHandler(RoutingContext req) {
    JsonObject jsonBody = req.getBodyAsJson();
    Service service = new Service(jsonBody.getString("name"), jsonBody.getString("url"), "test");
    connector.addService(service).setHandler(s -> {
      if (s.failed()) {
        req.response().setStatusCode(500).setStatusMessage("Unable to add service to database");
      } else {
        req.response().putHeader("content-type", "text/plain").end("OK");
      }
    });
  }

  private void deleteServicesHandler(RoutingContext req) {
    JsonObject jsonBody = req.getBodyAsJson();
    String serviceName = jsonBody.getString("name");
    connector.deleteService(serviceName).setHandler(s -> {
      if (s.failed()) {
        req.response().setStatusCode(404).setStatusMessage("No service by that name exists");
      } else {
        req.response().putHeader("content-type", "text/plain").end("OK");
      }
    });
  }

}



