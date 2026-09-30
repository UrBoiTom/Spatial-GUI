package org.tastytrash.spatialGUI.mixin.gui;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

@Mixin(Screen.class)
public class ScreenMixin {
    //? if >=26.1.2 {
    /*@Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
            *///?} else {
    @Inject(method = "renderTransparentBackground", at = @At("HEAD"), cancellable = true)
     //?}
    private void spatialGUI$removeBackgroundOverlay(CallbackInfo ci) {
        if (SpatialGUIClient.renderer().shouldCapture() && SpatialGUI.config.enabled) {
            ci.cancel();
        }
    }
}