package org.tastytrash.spatialGUI.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;

//? if <26.1.2 {
import com.llamalad7.mixinextras.sugar.Local;
 //?}

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    //? if >=26.2 {
    /*@Inject(method = "render", at = @At("TAIL"))
     *///?} else {
    @Inject(method = "renderLevel", at = @At("TAIL"))
            //?}
    private void diegeticInventory$renderScreen(CallbackInfo ci
            //? if <26.1.2 {
            , @Local(argsOnly = true, ordinal = 0) Matrix4f modelViewMatrix
            //?}
    ) {
        var renderer = SpatialGUIClient.renderer();
        if (!SpatialGUI.config.enabled || renderer == null || !renderer.shouldCapture()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        //? if >=26.1.2 {
        /*//? if >=26.2 {
        /^var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         ^///?} else {
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        //?}
        poseStack.mulPose(new Quaternionf()
                .rotateX((float) Math.toRadians(camera.getXRot()))
                .rotateY((float) Math.toRadians(camera.getYRot() + 180.0f))
                .get(new Matrix4f())
        );
        *///?} else {
        poseStack.mulPose(modelViewMatrix);
         //?}
        renderer.renderInWorld(poseStack);
    }
}