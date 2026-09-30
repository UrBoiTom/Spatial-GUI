package org.tastytrash.spatialGUI.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;

//? if >1.21.1 {
/*import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.render.SpatialGUIRenderer;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @ModifyArg(
            method = "draw", at = @At(value = "INVOKE",
                    //? if > 26.2 {
                    /^target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;II)V"
                    ^///? } else if 26.2 {
                    /^target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;II)V"
                    ^///? } else {
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;executeDrawRange(Ljava/util/function/Supplier;Lcom/mojang/blaze3d/pipeline/RenderTarget;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;II)V"
                    //?}

            ), index = 1)
    private RenderTarget diegeticInventory$redirectRenderTarget(RenderTarget original) {
        // makes Minecraft render to the framebuffer instead of the normal GUI
        SpatialGUIRenderer renderer = SpatialGUIClient.renderer();

        if (!SpatialGUI.config.enabled) {
            return original;
        }

        if ((Object) this == renderer.getScreenGuiRenderer()) {
            return renderer.getTargetManager().getTarget();
        }

        return original;
    }
}
*///?} else {
import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
public class GuiRendererMixin {
}
//?}