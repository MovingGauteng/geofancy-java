package za.co.movinggauteng.geofancy

import io.github.cdimascio.dotenv.dotenv
import io.jaegertracing.Configuration
import io.opentracing.util.GlobalTracer
import za.co.movinggauteng.geofancy.grpc.*

val dotenv = dotenv {
    ignoreIfMalformed = true
    ignoreIfMissing = true
}

fun main(args: Array<String>) {

    val tracerConfig = Configuration("geofancy-java")
    tracerConfig.withReporter(Configuration.ReporterConfiguration()
            .withSender(Configuration.SenderConfiguration()
                    .withEndpoint(dotenv["OPENTRACING_ENDPOINT"])))
    val tracer = tracerConfig.tracer

    GlobalTracer.register(tracer)

    val server = GeofancyServer((dotenv["GRPC_SERVER_PORT"] ?: "9003").toInt())
    server.start()

    server.blockUntilShutdown()
}