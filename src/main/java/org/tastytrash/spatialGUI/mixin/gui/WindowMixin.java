package org.tastytrash.spatialGUI.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyReturnValue(method = "getGuiScale", at = @At("RETURN"))
    //? if >1.21.1 {
    /*private int spatialGUI$overrideGuiScale(int original) {
        return shouldOverride() ? getGuiScale() : original;
    }
    *///?} else {
    private double spatialGUI$overrideGuiScale(double original) {
        return shouldOverride() ? (double) getGuiScale() : original;
    }
    //?}

    @ModifyReturnValue(method = "getGuiScaledWidth", at = @At("RETURN"))
    private int spatialGUI$overrideScaledWidth(int original) {
        return shouldOverride() ? calculateScaledDimension(true) : original;
    }

    @ModifyReturnValue(method = "getGuiScaledHeight", at = @At("RETURN"))
    private int spatialGUI$overrideScaledHeight(int original) {
        return shouldOverride() ? calculateScaledDimension(false) : original;
    }

    @Unique
    private int calculateScaledDimension(boolean isWidth) {
        Window self = (Window)(Object) this;
        double dimension = isWidth ? self.getWidth() : self.getHeight();
        return Mth.ceil(dimension / (double) getGuiScale());
    }

    @Unique
    private int getGuiScale() {
        Window self = (Window)(Object) this;
        return SpatialGUI.config.getEffectiveGuiScale(self.getHeight());
    }

    @Unique
    private static boolean shouldOverride() {
        if (!SpatialGUI.config.enabled || SpatialGUIRenderer.skipWindowOverride) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui == null) return false;
        //? if >=26.2 {
        /*Screen screen = mc.gui.screen();
        *///?} else {
        Screen screen = mc.screen;
        //?}
        if (screen instanceof AbstractContainerScreen<?>) return true;
        var renderer = org.tastytrash.spatialGUI.client.SpatialGUIClient.renderer();
        return renderer != null && renderer.getHookedScreen() instanceof AbstractContainerScreen<?>;
    }
}