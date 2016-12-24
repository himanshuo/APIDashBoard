package io.vertx.blog.first;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import java.net.ServerSocket;
import io.vertx.ext.web.Router;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.handler.StaticHandler;
import java.util.Map;
import java.util.LinkedHashMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.Json;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.Logger;


public class MyFirstVerticle extends AbstractVerticle {
    // extending from AbstractVerticle gives us vertx protected variable

    //todo (himanshuo): start is a state of a verticle. what are other states?
    //todo (himanshuo): what is execution order of verticle states?

    // Store our product
    private Map<Integer, Whisky> products = new LinkedHashMap<>();

    // Create some product
    private void createSomeSampleData() {
      Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
      products.put(bowmore.getId(), bowmore);
      Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");
      products.put(talisker.getId(), talisker);
    }

    // GET /api/whiskies
    // get all whiskies
    private void getAll(RoutingContext routingContext) {
      routingContext.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(products.values()));
    }

    // GET /api/whiskies/:id
    // get a Whisky with :id
    private void getOne(RoutingContext routingContext) {
      String id = routingContext.request().getParam("id");
      if (id == null) {
        routingContext.response().setStatusCode(400).end();
      } else {
        Integer idAsInteger = Integer.valueOf(id);
        routingContext.response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(products.get(idAsInteger)));
      }
    }

    // POST /api/whiskies
    // POST /api/whiskies*
    // create and store a Whisky object
    private void addOne(RoutingContext routingContext) {
      final Whisky whisky = Json.decodeValue(routingContext.getBodyAsString(),
          Whisky.class);
      products.put(whisky.getId(), whisky);
      routingContext.response()
          .setStatusCode(201)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(whisky));
    }

    // PUT /api/whiskies/:id
    // update a whisky object
    private void updateOne(RoutingContext routingContext) {
      logger.info(routingContext.getBodyAsString());
      logger.info("id:" +routingContext.request().getParam("id"));
      logger.info("fileupload size:" +routingContext.fileUploads().size());

      final Whisky newValue = Json.decodeValue(routingContext.getBodyAsString(),
        Whisky.class);
      String id = routingContext.request().getParam("id");
      if(id == null) {
        routingContext.response().setStatusCode(400).end();
      } else {
        int intId = Integer.parseInt(id);


        System.out.printf("INPUT: %s\n", newValue);
        Whisky old = products.get(intId);
        System.out.printf("%d => %s\n", intId, old);
        products.remove(intId);
        products.put(intId, newValue);
        System.out.println("UPDATED");
        System.out.printf("%d => %s\n", intId, products.get(intId));
        routingContext.response()
          .setStatusCode(202)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(products.get(intId)));
      }
    }

    // DELETE /api/whiskies/:id
    // delete a Whisky Object with :id
    private void deleteOne(RoutingContext routingContext) {
      String id = routingContext.request().getParam("id");
      if (id == null) {
        routingContext.response().setStatusCode(400).end();
      } else {
        Integer idAsInteger = Integer.valueOf(id);
        products.remove(idAsInteger);
      }
      routingContext.response().setStatusCode(204).end();
    }


    static int port;
    Logger logger = LoggerFactory.getLogger("");

    //start is run when verticle starts
    @Override
    public void start(Future<Void> fut) throws java.io.IOException {
        //fut helps to determine state of Verticle
        //fut.complete() is successful run
        //fut.fail() is fail run

        createSomeSampleData();

        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message - so we are still compatible.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader("content-type", "text/html")
                    .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });

        ServerSocket socket = new ServerSocket(0);
        int potentialPort = socket.getLocalPort();
        socket.close();
        port = config().getInteger("http.port", potentialPort);
        port = 8080;
        // ******************* ROUTES *****************************
        //static assets
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        //whiskies
        router.get("/api/whiskies").handler(this::getAll);
        router.put("/api/whiskies/:id").handler(this::updateOne);
        router.route("/api/whiskies*").handler(BodyHandler.create());
        router.post("/api/whiskies").handler(this::addOne);
        router.delete("/api/whiskies/:id").handler(this::deleteOne);
        // ********************************************************

        vertx
                .createHttpServer()
                .requestHandler(router::accept) //object::myfunc is a pointer to myfunc inside the object
                .listen( port, result -> {
                    if (result.succeeded()) {
                        System.out.printf("Listening on port %d\n", port);
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}
