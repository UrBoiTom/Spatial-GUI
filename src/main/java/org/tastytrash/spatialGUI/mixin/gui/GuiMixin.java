package org.tastytrash.spatialGUI.mixin.gui;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void spatialGUI$beginExtract(CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = true;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void spatialGUI$endExtract(CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = false;
    }

    //? if fabric && >=26.2 {
    /^@Redirect(method = "extractRenderState", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
    ))
    private void spatialGUI$redirectScreenExtraction(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        var renderer = SpatialGUIClient.renderer();
        if (SpatialGUI.config.enabled && screen instanceof AbstractContainerScreen<?> && screen == renderer.getHookedScreen()) {
            SpatialGUIRenderer.skipWindowOverride = false;
            renderer.extractIsolatedScreen(screen, partialTick);
            SpatialGUIRenderer.skipWindowOverride = true;
        } else {
            screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        }
    }
    ^///? }
}
*///?} else if >=1.21.1 {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void spatialGUI$beginGuiRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void spatialGUI$endGuiRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = false;
    }
}
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void spatialGUI$beginGuiRender(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void spatialGUI$endGuiRender(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        SpatialGUIRenderer.skipWindowOverride = false;
    }
}
//?}