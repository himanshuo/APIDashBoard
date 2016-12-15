package io.vertx.blog.first;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class MyFirstVerticle extends AbstractVerticle {
    // extending from AbstractVerticle gives us vertx protected variable

    //todo (himanshuo): start is a state of a verticle. what are other states?
    //todo (himanshuo): what is execution order of verticle states?

    //start is run when verticle starts
    @Override
    public void start(Future<Void> fut) {
        //fut helps to determine state of Verticle
        //fut.complete() is successful run
        //fut.fail() is fail run


        vertx
                .createHttpServer()
                .requestHandler(r -> {
                    r.response().end("<h1>Hello from my first " +
                            "Vert.x 3 application</h1>");
                })
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        fut.complete();
                    } else {
                        fut.fail(result.cause());
                    }
                });
    }
}