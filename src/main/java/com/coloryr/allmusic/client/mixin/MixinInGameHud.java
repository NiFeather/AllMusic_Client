package com.coloryr.allmusic.client.mixin;

import com.coloryr.allmusic.client.AllMusic;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud
{
    @Inject(method = "<init>",
            at = @At("RETURN"))
    public void allmusic$initLayer(MinecraftClient client, CallbackInfo ci, @Local(ordinal = 0) LayeredDrawer layeredDrawer)
    {
        layeredDrawer.addLayer(((context, tickCounter) ->
        {
            AllMusic.runIfInstancePresent(am -> am.mainRenderer.onRender(context));
        }));
    }
}
