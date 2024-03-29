package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new MainVerticle("test.db"), testContext.succeeding(id -> testContext.completeNow()));
        DBConnector connector = new DBConnector(vertx, "test.db");
        connector.query("DELETE FROM service");
    }

    @Test
    @DisplayName("Start a web server on localhost responding to path /service on port 8080")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void start_http_server(Vertx vertx, VertxTestContext testContext) {
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertEquals(0, body.size());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Do a full add, get, delete of a service and make sure it disappears afterwards")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void full_round_test(Vertx vertx, VertxTestContext testContext) {
        WebClient client = WebClient.create(vertx);
        client.post(8080, "::1", "/service")
                .sendJsonObject(new JsonObject().put("name", "kry").put("url", "www.kry.com"),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            assertEquals("OK", response.result().bodyAsString());
                            testContext.completeNow();
                }));
        client.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertEquals(1, body.size());
                    assertEquals("kry", body.getJsonObject(0).getString("name"));
                    assertEquals("UNKNOWN", body.getJsonObject(0).getString("status"));
                    testContext.completeNow();
                }));
        client.delete(8080, "::1", "/service")
                .sendJsonObject(new JsonObject().put("name", "kry"),
                        response -> testContext.verify(() -> {
                            assertEquals(200, response.result().statusCode());
                            assertEquals("OK", response.result().bodyAsString());
                            testContext.completeNow();
                        }));
        client.get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertEquals(0, body.size());
                    testContext.completeNow();
                }));
    }

}
