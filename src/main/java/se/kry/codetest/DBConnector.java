package se.kry.codetest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DBConnector {

  private final SQLClient client;

  public DBConnector(Vertx vertx, String databasePath){
    JsonObject config = new JsonObject()
        .put("url", "jdbc:sqlite:" + databasePath)
        .put("driver_class", "org.sqlite.JDBC")
        .put("max_pool_size", 30);

    client = JDBCClient.createShared(vertx, config);
  }

  public void close() {
    client.close();
  }

  public Future<ResultSet> query(String query) {
    return query(query, new JsonArray());
  }

  public Future<ResultSet> query(String query, JsonArray params) {
    if(query == null || query.isEmpty()) {
      return Future.failedFuture("Query is null or empty");
    }
    if(!query.endsWith(";")) {
      query = query + ";";
    }

    Future<ResultSet> queryResultFuture = Future.future();

    client.queryWithParams(query, params, result -> {
      if(result.failed()){
        queryResultFuture.fail(result.cause());
      } else {
        queryResultFuture.complete(result.result());
      }
    });
    return queryResultFuture;
  }

  public Future<ResultSet> addService(Service service) {
    if (service.getName() == null || service.getName().isEmpty()) {
      return Future.failedFuture("Name can't be empty");
    }
    return query("INSERT INTO service (name, url, added_by, last_status) VALUES (?, ?, ?, ?)",
            new JsonArray().add(service.getName()).add(service.getUrl()).add(service.getAddedBy()).add(service.getStatus())).setHandler(ar -> {
              if (ar.failed()) {
                ar.cause().printStackTrace();
              } else {
                System.out.println("Insert succeeded for service " + service.getName());
              }
    });
  }

  public Future<List<Service>> getAllServices() {
    return query("SELECT * FROM service").map(rs -> rs.getResults().stream().map(this::mapper).collect(Collectors.toList()));
  }

  public Future<Object> deleteService(String serviceName) {
    query("DELETE from service WHERE name = ?", new JsonArray().add(serviceName)).setHandler(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println("Delete succeeded for service " + serviceName);
      }
    });
    return Future.succeededFuture();
  }

  public Future<ResultSet> setServiceStatus(String serviceName, String status) {
    return query("UPDATE service SET last_status = ? WHERE name = ?",
            new JsonArray().add(status).add(serviceName)).setHandler(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      } else {
        System.out.println("Updated service " + serviceName + " with new status " + status);
      }
    });
  }

  private Service mapper(JsonArray row) {
    SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date created_at = null;
    try {
      created_at = ft.parse(row.getString(5));
    } catch (ParseException e) {
      System.out.println("Incorrect date format in database");
      e.printStackTrace();
    }
    return new Service(row.getString(1), row.getString(2), row.getString(3),
            created_at, row.getString(4));
  }

}
