package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    createTables(vertx, new DBConnector(Vertx.vertx(), "poller.db"));
    vertx = Vertx.vertx();
    createTables(vertx, new DBConnector(vertx, "test.db"));
  }

  private static void createTables(Vertx vertx, DBConnector connector) {
    connector.query("DROP TABLE IF EXISTS service");
    connector.query("CREATE TABLE IF NOT EXISTS service (" +
            "id INTEGER PRIMARY KEY," +
            "name VARCHAR(128) NOT NULL, " +
            "url VARCHAR(128) NOT NULL," +
            "added_by VARCHAR(128) NOT NULL," +
            "last_status VARCHAR(128) NOT NULL," +
            "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ");").setHandler(done -> {
      if(done.succeeded()){
        System.out.println("completed db migrations");
      } else {
        done.cause().printStackTrace();
      }
      vertx.close(shutdown -> {
        System.exit(0);
      });
    });
  }
}
