package za.co.movinggauteng.geofancy.grpc

import io.grpc.*
import io.grpc.stub.StreamObserver
import io.opentracing.contrib.grpc.ServerTracingInterceptor
import io.opentracing.util.GlobalTracer
import za.co.movinggauteng.geofancy.geoclient.Tile38Client
import za.co.movinggauteng.protos.geofancy.*
import za.co.movinggauteng.protos.geofancy.Document.GeoCase.*
import java.util.logging.Logger

class GeofancyServer(serverBuilder: ServerBuilder<*>, port: Int) {
    private var port: Int = 0
    private val server: Server

    constructor(port: Int) : this(ServerBuilder.forPort(port), port) {}

    init {
        this.port = port

        val tracingInterceptor = ServerTracingInterceptor(GlobalTracer.get())

        serverBuilder.addService(ServerInterceptors.intercept(GeoFancyService(), tracingInterceptor))

        server = serverBuilder.build()
    }

    fun start() {
        server.start()
        // lgger?
        Runtime.getRuntime().addShutdownHook(object: Thread() {
            override fun run() {
                System.err.println("*** Shutting down gRPC server as JVM is shutting down")
                this@GeofancyServer.stop()
                System.err.println("*** server shut down")
            }
        })
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class GeoFancyService: GeofancyServiceGrpc.GeofancyServiceImplBase() {

        override fun setDocument(request: Document, responseObserver: StreamObserver<CommandResult>) {

            when (request.geoCase!!) {

                POINT -> {
                    redisClient.setPoint(request.collection, request.id, request.point)
                    responseObserver.onNext(CommandResult.getDefaultInstance())
                    responseObserver.onCompleted()
                }
                LINE -> {
                    responseObserver.onError(StatusRuntimeException(Status.INVALID_ARGUMENT))
                }
                BOUNDS -> {
                    responseObserver.onError(StatusRuntimeException(Status.INVALID_ARGUMENT))
                }
                GEOJSON -> {
                    redisClient.setObject(request.collection, request.id, request.geojson)
                    responseObserver.onNext(CommandResult.getDefaultInstance())
                    responseObserver.onCompleted()
                }
                GEO_NOT_SET -> {
                    responseObserver.onError(StatusRuntimeException(Status.INVALID_ARGUMENT))
                }
            }
        }

        override fun deleteDocument(request: Document, responseObserver: StreamObserver<CommandResult>) {
            redisClient.deleteDocument(request.collection, request.id)
            responseObserver.onNext(CommandResult.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun deleteCollection(request: Document, responseObserver: StreamObserver<CommandResult>) {
            redisClient.deleteCollection(request.collection)
            responseObserver.onNext(CommandResult.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun deleteWebHook(request: SearchString, responseObserver: StreamObserver<CommandResult>) {
            logger.info("Attempt to delete webhook")
            redisClient.deleteWebhook(request)

            responseObserver.onNext(CommandResult.getDefaultInstance())
            responseObserver.onCompleted()
        }

        override fun createWebHook(request: GeoFence, responseObserver: StreamObserver<CommandResult>) {
            logger.info("Webhook results: ${redisClient.setWebhook(request)}")
            responseObserver.onNext(CommandResult.getDefaultInstance())
            responseObserver.onCompleted()
        }
    }

    companion object {
        private val redisClient = Tile38Client()
        private val logger = Logger.getLogger(this@Companion::class.java.simpleName)
    }

}