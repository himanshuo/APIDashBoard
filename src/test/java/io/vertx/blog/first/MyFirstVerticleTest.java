package io.vertx.blog.first;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.json.Json;


@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {

    private Vertx vertx;

    //@Before takes in TestContext which allows you to control asynchronous nature of verticle
    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        //deploy verticle
        vertx.deployVerticle(MyFirstVerticle.class.getName(),
                context.asyncAssertSuccess());
        //tests should all fail if verticle cant be started, thus context.asyncAssertSuccesss()
        //'start' = fut.complete() is called in the verticle's start function
    }


    @After
    public void tearDown(TestContext context) {
        // it seems that context can be applied like a callback to vertx.deployVerticle
        // and vertx.close
        // in both cases, context.asyncAssertSuccess makes sure the function succeeded
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) {
        // final means that the async cannot be reassigned. Indefinitly points to same object
        // todo (himanshuo): TestContext context is doing a lot of magic. understand it.
        // async is a 'async handle' which allows you to notify test framework when test is completed using async.complete()
        // todo (himanshuo): async handle?

        final Async async = context.async();

        int port = MyFirstVerticle.port;
        //getNow is shortcut for get(...).end()
        vertx.createHttpClient().getNow(port, "localhost", "/",
                //callback takes in response and calls response.handler with another callback with contents of response
                response -> {
                    // body is a buffer object thus you have to call toString to read full buffer
                    response.handler(body -> {
                        //assertion is done by context!
                        context.assertTrue(body.toString().contains("Hello"));
                        async.complete();
                    });
                });
    }

  @Test
  public void checkThatTheIndexPageIsServed(TestContext context) {
    Async async = context.async();
    vertx.createHttpClient().getNow(MyFirstVerticle.port, "localhost", "/assets/index.html", response -> {
      context.assertEquals(response.statusCode(), 200);
      context.assertEquals(response.headers().get("content-type"), "text/html");
      response.bodyHandler(body -> {
        context.assertTrue(body.toString().contains("<title>My Whisky Collection</title>"));
        async.complete();
      });
    });
  }

  @Test
  public void checkThatWeCanAdd(TestContext context) {
    Async async = context.async();
    final String json = Json.encodePrettily(new Whisky("Jameson", "Ireland"));
    final String length = Integer.toString(json.length());
    vertx.createHttpClient().post(MyFirstVerticle.port, "localhost", "/api/whiskies")
        .putHeader("content-type", "application/json")
        .putHeader("content-length", length)
        .handler(response -> {
          context.assertEquals(response.statusCode(), 201);
          context.assertTrue(response.headers().get("content-type").contains("application/json"));
          response.bodyHandler(body -> {
            final Whisky whisky = Json.decodeValue(body.toString(), Whisky.class);
            context.assertEquals(whisky.getName(), "Jameson");
            context.assertEquals(whisky.getOrigin(), "Ireland");
            context.assertNotNull(whisky.getId());
            async.complete();
          });
        })
        .write(json)
        .end();
  }
}
