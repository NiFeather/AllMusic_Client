package com.coloryr.allmusic.client.graphics;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.hud.HudAnchor;
import com.coloryr.allmusic.client.hud.MusicMeta;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainRenderer
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MainRenderer.class);

    private final ScheduledExecutorService timerExecutor;

    public MainRenderer()
    {
        this.timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(this::timer, 0, 1, TimeUnit.MILLISECONDS);
    }

    //region Timer

    private int count;

    // 渲染图片时的旋转角度
    private int renderAngle = 0;

    private void timer()
    {
        if (musicMetadata == null)
        {
            renderAngle = 0;
            return;
        }

        if (!musicMetadata.pic.shadow)
        {
            renderAngle = 0;
            return;
        }

        if (count < musicMetadata.picRotateSpeed)
        {
            count++;
            return;
        }

        count = 0;
        renderAngle++;
        renderAngle = renderAngle % 360;
    }

    //endregion Timer

    public void onRender(DrawContext context)
    {
        this.doRender(context);
    }

    private MusicMeta musicMetadata;

    public void setMusicMetadata(MusicMeta musicMetadata)
    {
        this.musicMetadata = musicMetadata;
    }

    public MusicMeta getMusicMetadata()
    {
        return musicMetadata;
    }

    public String infoDisplay = "";
    public String listDisplay = "";
    public String lyricDisplay = "";

    public void resetDisplay()
    {
        infoDisplay = "";
        listDisplay = "";
        lyricDisplay = "";

        musicMetadata = new MusicMeta();
    }

    @Nullable
    private Identifier currentTexture;

    @Nullable
    private Identifier textureRounded;

    public void setCurrentTexture(@Nullable Identifier newTexture, @Nullable Identifier newTextureRounded)
    {
        var textureManager = AllMusic.instance().webTextureManager;

        textureManager.destroy(currentTexture);
        textureManager.destroy(textureRounded);

        this.currentTexture = newTexture;
        this.textureRounded = newTextureRounded;
    }

    @Nullable
    public Identifier getCurrentTexture()
    {
        return currentTexture;
    }

    public void doRender(DrawContext context)
    {
        MusicMeta musicMeta = this.musicMetadata;
        if (musicMeta == null) return;

        if (musicMeta.info.enable && !infoDisplay.isEmpty())
        {
            int offset = 0;

            String[] temp = infoDisplay.split("\n");

            for (String item : temp)
            {
                drawText(context, item, musicMeta.info.x, musicMeta.info.y + offset,
                        musicMeta.info.dir, musicMeta.info.color, musicMeta.info.shadow);

                offset += 10;
            }
        }

        if (musicMeta.list.enable && !listDisplay.isEmpty())
        {
            String[] temp = listDisplay.split("\n");
            int offset = 0;
            for (String item : temp)
            {
                drawText(context, item, musicMeta.list.x, musicMeta.list.y + offset,
                        musicMeta.list.dir, musicMeta.list.color, musicMeta.list.shadow);

                offset += 10;
            }
        }

        if (musicMeta.lyric.enable && !lyricDisplay.isEmpty())
        {
            String[] temp = lyricDisplay.split("\n");

            int offset = 0;
            for (String item : temp)
            {
                drawText(context, item,
                        musicMeta.lyric.x, musicMeta.lyric.y + offset,
                        musicMeta.lyric.dir, musicMeta.lyric.color, musicMeta.lyric.shadow);

                offset += 10;
            }
        }

        //if (musicMeta.pic.enable && haveImg)
        if (musicMeta.pic.enable)
        {
            // pic.shadow 是是否旋转； pic.color 是封面大小
            var identifier = musicMeta.pic.shadow ? textureRounded : currentTexture;

            if (identifier != null)
                drawPicture(context, identifier , musicMeta.pic.color, musicMeta.pic.x, musicMeta.pic.y, musicMeta.pic.dir, renderAngle);
        }
    }

    private void drawText(DrawContext context, String text, int x, int y, HudAnchor dir, int color, boolean shadow)
    {
        int width = AllMusic.getTextWidth(text);
        int height = AllMusic.getFontHeight();

        int screenWidth = AllMusic.getScreenWidth();
        int screenHeight = AllMusic.getScreenHeight();

        int x1 = x;
        int y1 = y;

        switch (dir)
        {
            case TOP_CENTER:
                x1 = screenWidth / 2 - width / 2 + x;
                break;

            case TOP_RIGHT:
                x1 = screenWidth - width - x;
                break;

            case LEFT:
                y1 = screenHeight / 2 - height / 2 + y;
                break;

            case CENTER:
                x1 = screenWidth / 2 - width / 2 + x;
                y1 = screenHeight / 2 - height / 2 + y;
                break;

            case RIGHT:
                x1 = screenWidth - width - x;
                y1 = screenHeight / 2 - height / 2 + y;
                break;

            case BOTTOM_LEFT:
                y1 = screenHeight - height - y;
                break;

            case BOTTOM_CENTER:
                x1 = screenWidth / 2 - width / 2 + x;
                y1 = screenHeight - height - y;
                break;

            case BOTTOM_RIGHT:
                x1 = screenWidth - width - x;
                y1 = screenHeight - height - y;
                break;
        }

        context.drawText(MinecraftClient.getInstance().textRenderer, text, x1, y1, color, shadow);
    }

    private final int placeholderColor = ColorHelper.withAlpha(128, TextColor.parse("#333333").getOrThrow().getRgb());

    private void drawPicture(DrawContext context, @NotNull Identifier textureID, int renderSize, int x, int y, HudAnchor dir, int rotationAngle)
    {
        if (dir == null)
            return;

        int screenWidth = AllMusic.getScreenWidth();
        int screenHeight = AllMusic.getScreenHeight();

        int x1 = x;
        int y1 = y;

        switch (dir) {
            case TOP_CENTER:
                x1 = screenWidth / 2 - renderSize / 2 + x;
                break;
            case TOP_RIGHT:
                x1 = screenWidth - renderSize - x;
                break;
            case LEFT:
                y1 = screenHeight / 2 - renderSize / 2 + y;
                break;
            case CENTER:
                x1 = screenWidth / 2 - renderSize / 2 + x;
                y1 = screenHeight / 2 - renderSize / 2 + y;
                break;
            case RIGHT:
                x1 = screenWidth - renderSize - x;
                y1 = screenHeight / 2 - renderSize / 2 + y;
                break;
            case BOTTOM_LEFT:
                y1 = screenHeight - renderSize - y;
                break;
            case BOTTOM_CENTER:
                x1 = screenWidth / 2 - renderSize / 2 + x;
                y1 = screenHeight - renderSize - y;
                break;
            case BOTTOM_RIGHT:
                x1 = screenWidth - renderSize - x;
                y1 = screenHeight - renderSize - y;
                break;
        }

        var texture = AllMusic.instance().webTextureManager.getTexture(textureID);
        if (!(texture instanceof NativeImageBackedTexture nativeImageBackedTexture) || nativeImageBackedTexture.getImage() == null)
        {
            context.fill(x1, y1, x1 + renderSize, y1 + renderSize, placeholderColor);
            return;
        }

        var image = nativeImageBackedTexture.getImage();
        var imageWidth = image.getWidth();
        var imageHeight = image.getHeight();

        var matrices = context.getMatrices();

        matrices.push();

        // 初步位移
        // 但是不知道是从哪移到哪
        int centerOffset = renderSize / 2;
        matrices.translate(x1 + centerOffset, y1 + centerOffset, 0);

        // 应用宣传
        if (rotationAngle > 0)
            matrices.multiply(new Quaternionf().fromAxisAngleDeg(0, 0, 1, rotationAngle));

        // 缩放到我们想要的渲染大小
        float scaleFactor = renderSize / (float)imageHeight;
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);

        // 使图像居中
        float offset = (float)-imageHeight / 2;
        matrices.translate(offset, offset, 0);

        context.drawTexture(RenderLayer::getGuiTextured, textureID,
                0, 0, 0, 0,
                imageWidth, imageHeight,
                imageWidth, imageHeight
        );

        matrices.pop();
    }
}
