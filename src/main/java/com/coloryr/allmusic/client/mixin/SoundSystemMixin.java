package com.coloryr.allmusic.client.mixin;

import com.coloryr.allmusic.client.AllMusic;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin
{
    @Shadow public abstract void stop(SoundInstance sound);

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At("HEAD"), cancellable = true)
    public void allmusic$onPlay(SoundInstance soundInstance, CallbackInfoReturnable<SoundSystem.PlayResult> cir)
    {
        AllMusic.runIfInstancePresent(am ->
        {
            if (!am.player.playing()) return;

            SoundCategory data = soundInstance.getCategory();
            switch (data)
            {
                case RECORDS, MUSIC ->
                {
                    cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
                }
            }
        });
    }

    @Inject(method = "reloadSounds", at = @At("RETURN"))
    public void allmusic$onSoundReload(CallbackInfo info)
    {
        AllMusic.runIfInstancePresent(AllMusic::reload);
    }
}
