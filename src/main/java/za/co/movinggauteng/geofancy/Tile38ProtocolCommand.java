package za.co.movinggauteng.geofancy;


import io.lettuce.core.protocol.LettuceCharsets;
import io.lettuce.core.protocol.ProtocolKeyword;

public enum Tile38ProtocolCommand implements ProtocolKeyword
{
    SETHOOK("SETHOOK"),
    NEARBY("NEARBY"),
    DELHOOK("DELHOOK"),
    PDELHOOK("PDELHOOK"),
    DROP("DROP");

    private final byte[] name;

    Tile38ProtocolCommand(String commandName) {
        name = commandName.getBytes(LettuceCharsets.ASCII);
    }

    public byte[] getBytes() {
        return name;
    }
}