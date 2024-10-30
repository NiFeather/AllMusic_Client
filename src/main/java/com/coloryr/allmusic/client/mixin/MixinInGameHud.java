package com.coloryr.allmusic.client.mixin;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.graphics.MainRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Inject(method = {"renderStatusEffectOverlay"}, at = {@At(value = "HEAD")})
    public void allmusic$onRenderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        AllMusic.runIfInstancePresent(am -> am.mainRenderer.onRender(context));
    }
}
