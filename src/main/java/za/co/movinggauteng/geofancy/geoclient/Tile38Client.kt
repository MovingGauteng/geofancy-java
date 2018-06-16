package za.co.movinggauteng.geofancy.geoclient

import io.github.cdimascio.dotenv.dotenv
import io.lettuce.core.RedisClient
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.output.MapOutput
import io.lettuce.core.output.StatusOutput
import io.lettuce.core.protocol.CommandArgs
import io.lettuce.core.protocol.CommandType
import za.co.movinggauteng.geofancy.Tile38ProtocolCommand
import za.co.movinggauteng.protos.geofancy.Bounds
import za.co.movinggauteng.protos.geofancy.GeoFence
import za.co.movinggauteng.protos.geofancy.GeoFence.QueryCase.*
import za.co.movinggauteng.protos.geofancy.Point
import za.co.movinggauteng.protos.geofancy.SearchString

class Tile38Client {

    private val redisClient: RedisClient = RedisClient.create(env["TILE38_CONNECTION"])

    private val connection = redisClient.connect()

    private val sync: RedisCommands<String, String> = connection.sync()

    private val codec: StringCodec = StringCodec.UTF8

    companion object {
        private val env = dotenv()
    }

    fun setPoint(collection: String, id: String, point: Point) : String {
        return sync.dispatch(
                CommandType.SET,
                StatusOutput(codec),
                CommandArgs(codec)
                        .add(collection)
                        .add(id)
                        .add("POINT")
                        .add(point.coord.lat)
                        .add(point.coord.lng)
        )
    }

    fun setBounds(collection: String, id: String, bounds: Bounds) {
        TODO("Not yet implemented")
    }

    fun setWebhook(geofence: GeoFence): MutableMap<String, String>? {
        val commands = CommandArgs(codec)
        // load commands
        commands.add(geofence.id)
        commands.add(geofence.endpoint)
        when(geofence.queryCase) {

            NEARBY -> {
                commands.add(geofence.queryCase.name)
                commands.add(geofence.nearby.collection)
            }
            WITHIN -> TODO()
            INTERSECTS -> TODO()
            QUERY_NOT_SET -> TODO()
        }
        // filter
        commands.add("MATCH")
        commands.add(geofence.match)
        commands.add("FENCE")
        if (geofence.detectCount > 0) {
            commands.add("DETECT")
            commands.add(geofence.detectList.joinToString(",").toLowerCase())
        }
        if (geofence.commandsCount > 0) {
            commands.add("COMMANDS")
            commands.add(geofence.commandsList.joinToString(",").toLowerCase())
        }
        commands.add("POINT")
        commands.add(geofence.point.coord.lat)
        commands.add(geofence.point.coord.lng)
        commands.add(geofence.distance)

        return sync.dispatch(Tile38ProtocolCommand.SETHOOK, MapOutput(codec), commands)
    }

    fun setObject(collection: String, id: String, geoJson: String): String {
        return sync.dispatch(
                CommandType.SET,
                StatusOutput(codec),
                CommandArgs(codec)
                        .add(collection)
                        .add(id)
                        .add("OBJECT")
                        .add(geoJson)
        )
    }

    fun deleteDocument(collection: String, id: String): String {
        return sync.dispatch(
                CommandType.DEL,
                StatusOutput(codec),
                CommandArgs(codec)
                        .add(collection)
                        .add(id)
        )
    }

    fun deleteCollection(collection: String): String {
        return sync.dispatch(
                Tile38ProtocolCommand.DROP,
                StatusOutput(codec),
                CommandArgs(codec)
                        .add(collection)
        )
    }

    fun deleteWebhook(searchString: SearchString) : MutableMap<String, String>? {
        val commands = CommandArgs(codec)
        commands.add(searchString.value)

        return sync.dispatch(Tile38ProtocolCommand.PDELHOOK, MapOutput(codec), commands)
    }
}