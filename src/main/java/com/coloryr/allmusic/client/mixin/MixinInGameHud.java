package com.coloryr.allmusic.client.mixin;

import com.coloryr.allmusic.client.AllMusic;
import com.coloryr.allmusic.client.hud.MusicHud;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Unique
    private MusicHud allmusic$musicHud;

    @Inject(method = "<init>",
            at = @At("RETURN"))
    public void allmusic$initLayer(MinecraftClient client, CallbackInfo ci)
    {
        allmusic$musicHud = new MusicHud();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMainHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"
            )
    )
    public void allmusic$onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci)
    {
        allmusic$musicHud.render(context);
    }
}
