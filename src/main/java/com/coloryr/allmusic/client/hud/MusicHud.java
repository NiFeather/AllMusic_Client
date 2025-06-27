package com.coloryr.allmusic.client.hud;

import com.coloryr.allmusic.client.AllMusic;
import net.minecraft.client.gui.DrawContext;

public class MusicHud
{
    public void render(DrawContext context)
    {
        AllMusic.runIfInstancePresent(am -> am.mainRenderer.onRender(context));
    }
}
