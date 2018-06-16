# Geofancy-Java

Geofancy is a (hopefully) simple [gRPC](http://grpc.io) service that implements a subset of [Tile38](http://tile38.com).

This repository contains the Java/Kotlin implementation.

There is also another repository ([geofancy-rs](https://github.com/MovingGauteng/geofancy-rs)), which contains the Rust implementation of the server.

## Why Geofancy?

Tile38 is a great program, that amongst others, allows one to set geofences between moving and static geospatial objects; with the ability to trigger webhooks when conditions are met.

While we have tried out Tile38, we didn't want to implement all the logic that we wanted within some of our services, so we decided to create a separate service instead.

Geofancy is a stand-alone microservice that connects with Tile38, and exposes a subset of its API via a gRPC service.

### Implemented Methods

You can view the `geofancy.proto` , which lists the RPCs that are supported.

At the time of writing (imagine a blog post with no date having this line) ... The API is still unstable, and can evolve.
We have

```proto
service GeofancyService {
    rpc CreateWebHook (GeoFence) returns (CommandResult) {}
    rpc DeleteWebHook (SearchString) returns (CommandResult) {}
    rpc SetDocument (Document) returns (CommandResult) {}
    rpc DeleteDocument (Document) returns (CommandResult) {}
    rpc DeleteCollection (Document) returns (CommandResult) {}
}
```

Notice that we return a very simple `CommandResult` message. Contributions are welcome, if anyone would like to return something more solid as a result.

### Example

We have included a `GeofancyClient` stub, which you can use to try out the API.

```kotlin

import za.co.movinggauteng.protos.geofancy.Coordinate
import za.co.movinggauteng.protos.geofancy.GeoFence
import za.co.movinggauteng.protos.geofancy.Point

fun main(args: Array<String>) {
    val geofancyClient = GeofancyClient(dotenv["GRPC_SERVER_HOST"] ?: "localhost", (dotenv["GRPC_SERVER_PORT"] ?: "5003").toInt())

    geofancyClient.getBlockingStub().createWebHook(GeoFence.newBuilder()
            .setId("the-id-that-will-get-triggered-by-a-webhook")
            .setEndpoint("http://localhost/webhook,grpc://localhost:9003")
            .setMatch("*")
            .setNearby(GeoFence.QueryNearby.newBuilder().setCollection("your-collection-name").build())
            .addAllDetect(listOf(GeoFence.Detect.ENTER, GeoFence.Detect.INSIDE))
            .addAllCommands(listOf(GeoFence.Commands.SET))
            .setPoint(Point.newBuilder().setCoord(Coordinate.newBuilder().setLat(-26.1).setLng(28.12)).build())
            .setDistance(200L)
            .build()
    )
}
```