package com.coloryr.allmusic.client.utils;

import net.minecraft.util.Identifier;

public class Utils
{
    public static Identifier nameIdentifierFrom(String string)
    {
        var hash = Integer.toHexString(string.hashCode());

        return Identifier.of("allmusic", hash);
    }
}
