package com.coloryr.allmusic.client.network;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.hud.ComType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

import java.nio.charset.StandardCharsets;

public record PacketPayload(ComType type, String data, int data1) implements CustomPayload
{
    public static final Id<PacketPayload> ID = new CustomPayload.Id<>(AllMusic.ID);

    private static String readString(PacketByteBuf buf)
    {
        int size = buf.readInt();
        byte[] temp = new byte[size];
        buf.readBytes(temp);

        return new String(temp, StandardCharsets.UTF_8);
    }

    public static final PacketCodec<PacketByteBuf, PacketPayload> CODEC = PacketCodec.of
            (
                    (value, buf) -> { throw new RuntimeException("Not implemented!"); },
                    PacketPayload::decode
            );

    private static PacketPayload decode(PacketByteBuf buffer)
    {
        byte type = buffer.readByte();

        if (type >= ComType.values().length || type < 0)
            return null;

        ComType type1 = ComType.values()[type];
        PacketPayload payload = new PacketPayload(ComType.clear, null, 0);

        switch (type1) {
            case lyric, info, list, play, img, hud -> payload = new PacketPayload(type1, readString(buffer), 0);
            case stop, clear -> payload = new PacketPayload(type1, null, 0);
            case pos -> payload = new PacketPayload(type1, null, buffer.readInt());
        }

        buffer.clear();
        return payload;
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return ID;
    }
}
