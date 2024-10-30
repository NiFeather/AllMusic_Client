package com.coloryr.allmusic.client.graphics;

import com.coloryr.allmusic.client.AllMusic;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WebTextureManager
{
    private static final Logger log = LoggerFactory.getLogger(WebTextureManager.class);
    private final HttpClient client;

    public WebTextureManager()
    {
        client = HttpClientBuilder.create()
                .useSystemProperties()
                .build();
    }

    private final List<Identifier> loadedTextures = new ObjectArrayList<>();

    /**
     * 从TextureManager获取材质
     * @param textureIdentifier
     * @return NULL if not found
     */
    @Nullable
    public AbstractTexture getTexture(Identifier textureIdentifier)
    {
        return MinecraftClient.getInstance().getTextureManager().getOrDefault(textureIdentifier, null);
    }

    private final Map<Identifier, CompletableFuture<Boolean>> onGoingRequests = new ConcurrentHashMap<>();

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight)
    {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics()
                .drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    /**
     * @return Whether success
     */
    public CompletableFuture<Boolean> fetch(Identifier targetIdentifier, String url)
    {
        var ongoing = onGoingRequests.getOrDefault(targetIdentifier, null);
        if (ongoing != null) return ongoing;

        var future = CompletableFuture.supplyAsync(() ->
        {
            var request = new HttpGet(url);
            HttpResponse response = null;

            try
            {
                response = client.execute(request);
                HttpEntity entity = response.getEntity();
                var contentStream = entity.getContent();

                var picSize = AllMusic.instance().configurations.currentConfig.picSize;

                // JPEG Stream
                var byteArrayStream = new ByteArrayInputStream(contentStream.readAllBytes());
                BufferedImage bufferedImage = ImageIO.read(byteArrayStream);

                // Convert to PNG
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", outputStream);

                var image = NativeImage.read(new ByteArrayInputStream(outputStream.toByteArray()));
                var texture = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(targetIdentifier, texture);

                this.loadedTextures.add(targetIdentifier);
            }
            catch (Throwable t)
            {
                log.error("未能获取图片数据：" + t.getMessage());
                t.printStackTrace();

                return false;
                //throw new RuntimeException(e);
            }

            return true;
        });

        future.thenRun(() -> onGoingRequests.remove(targetIdentifier));

        onGoingRequests.put(targetIdentifier, future);

        return future;
    }
}
