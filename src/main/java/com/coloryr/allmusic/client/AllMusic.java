package com.coloryr.allmusic.client;

import com.coloryr.allmusic.client.config.Configurations;
import com.coloryr.allmusic.client.graphics.MainRenderer;
import com.coloryr.allmusic.client.graphics.WebTextureManager;
import com.coloryr.allmusic.client.network.ServerHandler;
import com.coloryr.allmusic.client.player.APlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class AllMusic implements ModInitializer
{
    public static final Identifier ID = Identifier.of("allmusic", "channel");
    public APlayer player;

    public void stopPlaying()
    {
        try
        {
            player.closePlayer();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static int getScreenWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScreenHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    public static int getTextWidth(String item) {
        return MinecraftClient.getInstance().textRenderer.getWidth(item);
    }

    public static int getFontHeight() {
        return MinecraftClient.getInstance().textRenderer.fontHeight;
    }

    public static void sendMessage(String data)
    {
        MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(data)));
    }

    public static float getVolume()
    {
        return MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.RECORDS);
    }

    public void reload()
    {
        if (player != null)
            player.setReload();
    }

    public final MainRenderer mainRenderer = new MainRenderer();
    public final WebTextureManager webTextureManager = new WebTextureManager();
    public final ServerHandler serverHandler = new ServerHandler(this);
    public Configurations configurations;

    private static AllMusic instance;
    public static AllMusic instance()
    {
        return instance;
    }

    public static void runIfInstancePresent(Consumer<AllMusic> consumer)
    {
        if (instance == null) return;

        consumer.accept(instance);
    }

    private void tick(MinecraftClient client)
    {
        player.tick();
    }

    private void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client)
    {
        stopPlaying();
    }

    @Override
    public void onInitialize()
    {
        instance = this;
        configurations = new Configurations(FabricLoader.getInstance().getConfigDir());
        player = new APlayer();

        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
    }
}
