package com.coloryr.allmusic.client.mixin;

import com.coloryr.allmusic.client.AllMusic;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class SoundSystemMixin
{
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    public void allmusic$onPlay(SoundInstance soundInstance, CallbackInfo info)
    {
        AllMusic.runIfInstancePresent(am ->
        {
            if (!am.player.isPlay()) return;

            SoundCategory data = soundInstance.getCategory();
            switch (data)
            {
                case RECORDS, MUSIC -> info.cancel();
            }
        });
    }

    @Inject(method = "reloadSounds", at = @At("RETURN"))
    public void allmusic$onSoundReload(CallbackInfo info)
    {
        AllMusic.runIfInstancePresent(AllMusic::reload);
    }
}
