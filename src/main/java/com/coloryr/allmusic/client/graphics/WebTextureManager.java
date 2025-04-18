package com.coloryr.allmusic.client.graphics;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
        return MinecraftClient.getInstance().getTextureManager().getTexture(textureIdentifier);
    }

    public void destroy(@Nullable Identifier textureIdentifier)
    {
        if (textureIdentifier == null) return;

        MinecraftClient.getInstance().getTextureManager().destroyTexture(textureIdentifier);
    }

    private final Map<Identifier, CompletableFuture<Boolean>> onGoingRequests = new ConcurrentHashMap<>();

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

                // Possibly JPEG Stream
                var byteArrayStream = new ByteArrayInputStream(contentStream.readAllBytes());
                BufferedImage bufferedImage = ImageIO.read(byteArrayStream);

                registerImage(targetIdentifier, bufferedImage);

                var bufferedImageRounded = Utils.makePictureRounded(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage);
                registerImage(targetIdentifier.withSuffixedPath("_rounded"), bufferedImageRounded);

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

    private void registerImage(Identifier targetIdentifier, BufferedImage bufferedImage)
    {
        NativeImage image;

        try
        {
            // Convert to PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);

            image = NativeImage.read(new ByteArrayInputStream(outputStream.toByteArray()));
        }
        catch (Throwable t)
        {
            log.error("未能注册材质：" + t.getMessage());
            return;
        }

        var texture = new NativeImageBackedTexture(image);
        MinecraftClient.getInstance().getTextureManager().registerTexture(targetIdentifier, texture);
    }
}
