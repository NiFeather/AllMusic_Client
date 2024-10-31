package com.coloryr.allmusic.client.network;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.hud.MusicMeta;
import com.coloryr.allmusic.client.utils.Utils;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler
{
    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    private final AllMusic allMusic;

    public ServerHandler(AllMusic allMusicClient)
    {
        this.allMusic = allMusicClient;

        PayloadTypeRegistry.playS2C().register(PacketPayload.ID, PacketPayload.CODEC);

        var renderer = allMusicClient.mainRenderer;
        ClientPlayNetworking.registerGlobalReceiver(PacketPayload.ID, (payload, handler) ->
        {
            try
            {
                //log.info("Packet type is %s, data is '%s'".formatted(payload.type(), payload.data()));

                switch (payload.type())
                {
                    case lyric -> renderer.lyricDisplay = payload.data();
                    case info -> renderer.infoDisplay = payload.data();
                    case list -> renderer.listDisplay = payload.data();
                    case play ->
                    {
                        //log.info("PLAY! %s".formatted(payload.data()));
                        var soundManager = MinecraftClient.getInstance().getSoundManager();
                        soundManager.stopSounds(null, SoundCategory.MUSIC);
                        soundManager.stopSounds(null, SoundCategory.RECORDS);

                        allMusic.stopPlaying();
                        allMusic.player.setMusic(payload.data());
                    }
                    case img ->
                    {
                        var url = payload.data();
                        var identifier = Utils.nameIdentifierFrom(url);

                        allMusic.webTextureManager.fetch(identifier, url);
                        allMusic.mainRenderer.setCurrentTexture(identifier, identifier.withSuffixedPath("_rounded"));
                    }
                    case stop -> allMusic.stopPlaying();
                    case clear ->
                    {
                        allMusic.mainRenderer.setMusicMetadata(null);
                        allMusic.stopPlaying();
                        //hudUtils.close();
                    }
                    case pos ->
                    {
                        //log.info("Seek! " + payload.data1());
                        allMusic.player.set(payload.data1());
                    }
                    case hud ->
                    {
                        var decode = new Gson().fromJson(payload.data(), MusicMeta.class);
                        allMusic.mainRenderer.setMusicMetadata(decode);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
}
