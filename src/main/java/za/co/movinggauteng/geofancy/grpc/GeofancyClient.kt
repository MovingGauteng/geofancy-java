package za.co.movinggauteng.geofancy.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import za.co.movinggauteng.protos.geofancy.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GeofancyClient(channelBuilder: ManagedChannelBuilder<*>) {
    val channel: ManagedChannel = channelBuilder.build()
    private var greeterBlockingStub: GeofancyServiceGrpc.GeofancyServiceBlockingStub
    private var greeterStub: GeofancyServiceGrpc.GeofancyServiceStub

    constructor(host: String, port: Int, plainText: Boolean = true) : this(
            when(plainText) {
                true -> ManagedChannelBuilder.forAddress(host, port).usePlaintext().executor(Executors.newFixedThreadPool(16))
                false -> ManagedChannelBuilder.forAddress(host, port).executor(Executors.newFixedThreadPool(16))
            }
    )

    init {
        val metadata: Metadata = Metadata()
//        metadata.put(io.grpc.Metadata.Key.of("Authorization", io.grpc.Metadata.ASCII_STRING_MARSHALLER), "${accessToken.tokenType} ${accessToken.accessToken}")
        greeterBlockingStub = MetadataUtils.attachHeaders(GeofancyServiceGrpc.newBlockingStub(channel), metadata)
        greeterStub = MetadataUtils.attachHeaders(GeofancyServiceGrpc.newStub(channel), metadata)
    }

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun isShutdown() : Boolean = channel.isShutdown

    fun getBlockingStub() : GeofancyServiceGrpc.GeofancyServiceBlockingStub {
        if (channel.isShutdown) {
            val metadata: Metadata = Metadata()
//            metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "${accessToken!!.tokenType} ${accessToken!!.accessToken}")
            greeterBlockingStub = MetadataUtils.attachHeaders(GeofancyServiceGrpc.newBlockingStub(channel), metadata)
            return greeterBlockingStub
        }
        return greeterBlockingStub
    }

    fun getStub() : GeofancyServiceGrpc.GeofancyServiceStub {
        if (channel.isShutdown) {
            val metadata: Metadata = Metadata()
//            metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "${accessToken!!.tokenType} ${accessToken!!.accessToken}")
            greeterStub = MetadataUtils.attachHeaders(GeofancyServiceGrpc.newStub(channel), metadata)
            return greeterStub
        }
        return greeterStub
    }
}