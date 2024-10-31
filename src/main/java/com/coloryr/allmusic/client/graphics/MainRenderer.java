package com.coloryr.allmusic.client.graphics;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.hud.HudAnchor;
import com.coloryr.allmusic.client.hud.MusicMeta;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainRenderer
{
    private static final Logger log = LoggerFactory.getLogger(MainRenderer.class);

    private final ScheduledExecutorService executor;

    public MainRenderer()
    {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this::timer, 0, 1, TimeUnit.MILLISECONDS);
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

    @Nullable
    private Identifier currentTexture;

    @Nullable
    private Identifier textureRounded;

    public void setCurrentTexture(Identifier newTexture, Identifier newTextureRounded)
    {
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

    private void drawPicture(DrawContext context, Identifier textureID, int size, int x, int y, HudAnchor dir, int ang)
    {
        if (dir == null)
            return;

        int screenWidth = AllMusic.getScreenWidth();
        int screenHeight = AllMusic.getScreenHeight();

        int x1 = x;
        int y1 = y;

        switch (dir) {
            case TOP_CENTER:
                x1 = screenWidth / 2 - size / 2 + x;
                break;
            case TOP_RIGHT:
                x1 = screenWidth - size - x;
                break;
            case LEFT:
                y1 = screenHeight / 2 - size / 2 + y;
                break;
            case CENTER:
                x1 = screenWidth / 2 - size / 2 + x;
                y1 = screenHeight / 2 - size / 2 + y;
                break;
            case RIGHT:
                x1 = screenWidth - size - x;
                y1 = screenHeight / 2 - size / 2 + y;
                break;
            case BOTTOM_LEFT:
                y1 = screenHeight - size - y;
                break;
            case BOTTOM_CENTER:
                x1 = screenWidth / 2 - size / 2 + x;
                y1 = screenHeight - size - y;
                break;
            case BOTTOM_RIGHT:
                x1 = screenWidth - size - x;
                y1 = screenHeight - size - y;
                break;
        }

        var texture = AllMusic.instance().webTextureManager.getTexture(textureID);
        if (!(texture instanceof NativeImageBackedTexture nativeImageBackedTexture) || nativeImageBackedTexture.getImage() == null)
            return;

        var image = nativeImageBackedTexture.getImage();

        var matrices = context.getMatrices();

        matrices.push();

        float scaleFactor = size / (float)image.getHeight();
        matrices.scale(scaleFactor, scaleFactor, scaleFactor);

        drawPictureLegacy(texture.getGlId(), size, x1, y1, ang);

/*
        context.drawTexture(RenderLayer::getGuiTextured, textureID,
                x1, y1,
                0f, 0f,
                image.getWidth(), image.getHeight(),
                image.getWidth(), image.getHeight());
*/
        //drawPictureLegacy(texture.getGlId(), renderSize, x1, y1, ang);

        matrices.pop();
    }

    // 叫他Legacy是因为我想实现一个不需要这样的，尽可能简单直白的东西
    // 最主要是看不懂（划掉）看不顺眼这一堆奇怪的调用（
    private void drawPictureLegacy(int textureID, int size, int x, int y, int ang)
    {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        RenderSystem.setShaderTexture(0, textureID);

        MatrixStack stack = new MatrixStack();
        Matrix4f matrix = stack.peek().getPositionMatrix();

        int a = size / 2;

        if (ang > 0) {
            matrix = matrix.translationRotate(x + a, y + a, 0,
                    new Quaternionf().fromAxisAngleDeg(0, 0, 1, ang));
        } else {
            matrix = matrix.translation(x + a, y + a, 0);
        }

        int x0 = -a;
        int x1 = a;
        int y0 = -a;
        int y1 = a;
        int z = 0;
        int u0 = 0;
        float u1 = 1;
        float v0 = 0;
        float v1 = 1;

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix, (float) x0, (float) y1, (float) z).texture(u0, v1);
        bufferBuilder.vertex(matrix, (float) x1, (float) y1, (float) z).texture(u1, v1);
        bufferBuilder.vertex(matrix, (float) x1, (float) y0, (float) z).texture(u1, v0);
        bufferBuilder.vertex(matrix, (float) x0, (float) y0, (float) z).texture(u0, v0);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }
}
