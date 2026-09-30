package org.tastytrash.spatialGUI.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;
//? if >1.21.1 {
/*import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
*///?}
//? if >26.2 {
/*import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.StagedVertexBuffer;
*///?} else if 26.2 {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.StagedVertexBuffer;
*///?} else if >1.21.1 {
/*import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
*///?} else {
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
//?}
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.client.SpatialGUIClient;
import org.tastytrash.spatialGUI.util.AnimationUtil;
import org.tastytrash.spatialGUI.util.RenderUtil;

public class InventoryRenderer {
    private static final PoseStack WORLD_POSE_STACK = new PoseStack();
    //? if >1.21.1 {
    /*private static final RenderPipeline INVENTORY_PIPELINE = RenderPipelines.GUI_TEXTURED;
    private static final org.joml.Vector3f ZERO_VECTOR = new org.joml.Vector3f();
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
    private static final org.joml.Vector4f COLOR_MODULATOR = new org.joml.Vector4f();
     *///?}
    //? if >1.21.1 && <26.2 {
    /*private static final ByteBufferBuilder INVENTORY_BYTE_BUFFER = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    *///?}
    //? if >=26.2 {
    /*private static final StagedVertexBuffer INVENTORY_BUFFER = new StagedVertexBuffer(
            () -> "Spatial GUI Inventory Buffer",
            RenderType.SMALL_BUFFER_SIZE
    );
    *///?}

    private final TextureTargetManager targetManager;
    private QuadBasis quadBasis;
    private long screenOpenTime = 0;
    private static boolean isRecipeBookOpen = false;
    private static int recipeBookCloseDelay = 0;

    public InventoryRenderer(TextureTargetManager targetManager) {
        this.targetManager = targetManager;
    }

    public QuadBasis getQuadBasis() {
        return quadBasis;
    }

    public void setScreenOpenTime(long time) {
        this.screenOpenTime = time;
    }

    public void setRecipeBookOpen(boolean open) {
        isRecipeBookOpen = open;
        if (!open) {
            recipeBookCloseDelay = 0;
        }
    }

    public void resetRecipeBookState() {
        isRecipeBookOpen = false;
        recipeBookCloseDelay = 0;
    }

    //? if >=26.2 {
    /*public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();

        if (targetManager.getInventoryTarget() == null || !SpatialGUIClient.renderer().shouldCapture() || client.player == null) {
            return;
        }

        var texture = targetManager.getInventoryTarget().getColorTextureView();
        VertexFormat format = INVENTORY_PIPELINE.getVertexFormatBinding(0);

        if (texture == null || format == null) {
            return;
        }

        PrimitiveTopology primitive = INVENTORY_PIPELINE.getPrimitiveTopology();
        StagedVertexBuffer.Draw draw = INVENTORY_BUFFER.appendDraw(
                format,
                primitive,
                primitive == PrimitiveTopology.QUADS ? RenderSystem.getProjectionType().vertexSorting() : null
        );

        matrices.pushPose();

        var player = client.player;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        float yaw = player.getYRot();
        float pitch = isFirstPerson ? player.getXRot() : 0;
        float pitchClamp = (float) SpatialGUI.config.firstPersonPitchClamp;
        pitch = Math.clamp(pitch, -pitchClamp, pitchClamp);
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);

        float scale = AnimationUtil.calculateAnimatedScale(config.scale(), screenOpenTime, isRecipeBookOpen, recipeBookCloseDelay);
        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);
        matrices.scale(scale, scale, scale);

        Matrix4f pose = matrices.last().pose();
        VertexConsumer buffer = INVENTORY_BUFFER.getVertexBuilder(draw);

        float aspect = (float) targetManager.getInventoryTarget().width / (float) targetManager.getInventoryTarget().height;
        RenderUtil.addScreenQuad(buffer, pose, aspect);

        WORLD_POSE_STACK.setIdentity();
        RenderUtil.applyScreenTransform(WORLD_POSE_STACK, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        WORLD_POSE_STACK.scale(scale, scale, scale);
        Matrix4f worldPose = WORLD_POSE_STACK.last().pose();

        quadBasis = RenderUtil.computeQuadBasis(worldPose, aspect, scale);

        matrices.popPose();

        INVENTORY_BUFFER.upload();
        StagedVertexBuffer.ExecuteInfo info = INVENTORY_BUFFER.getExecuteInfo(draw);
        if (info == null) {
            INVENTORY_BUFFER.endFrame();
            return;
        }

        drawInventory(info, texture);
        INVENTORY_BUFFER.endFrame();
    }

    private void drawInventory(StagedVertexBuffer.ExecuteInfo info, GpuTextureView texture) {
        Minecraft client = Minecraft.getInstance();
        RenderTarget mainTarget = client.gameRenderer.mainRenderTarget();
        var output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        float fadeAlpha = SpatialGUI.config.enableFadeAnimation
                ? Math.min(1.0F, (System.currentTimeMillis() - screenOpenTime) / (float) SpatialGUI.config.fadeDurationMs)
                : 1.0F;
        COLOR_MODULATOR.set(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrixCopy(),
                COLOR_MODULATOR,
                ZERO_VECTOR,
                IDENTITY_MATRIX
        );

        FilterMode filterMode = SpatialGUI.config.useLinearFiltering ? FilterMode.LINEAR : FilterMode.NEAREST;

        try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "Spatial GUI",
                output,
                java.util.Optional.empty(),
                null,
                java.util.OptionalDouble.empty()
        )) {
            //? if >26.2 {
            /^renderPass.setPipeline(RenderSystem.getCompiledPipeline(INVENTORY_PIPELINE));
             ^///?} else {
            renderPass.setPipeline(InventoryRenderer.INVENTORY_PIPELINE);
            //?}
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            //? if >26.2 {
            /^renderPass.setUniform
             ^///?} else {
            renderPass.bindTexture
                    //?}
                            ("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                                    AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                                    filterMode, filterMode, SpatialGUI.config.useAnisotropicFiltering
                            ));
            renderPass.setVertexBuffer(0, info.vertexBuffer().slice());
            renderPass.setIndexBuffer(info.indexBuffer(), info.indexType());
            renderPass.drawIndexed(info.indexCount(), 1, info.firstIndex(), info.baseVertex(), 0);
        }
    }
    *///?} else if >1.21.1 {
    /*public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();

        if (targetManager.getInventoryTarget() == null || !SpatialGUIClient.renderer().shouldCapture() || client.player == null) {
            return;
        }

        var texture = targetManager.getInventoryTarget().getColorTextureView();
        if (texture == null) {
            return;
        }

        matrices.pushPose();

        var player = client.player;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        float yaw = player.getYRot();
        float pitch = isFirstPerson ? player.getXRot() : 0;
        float pitchClamp = (float) SpatialGUI.config.firstPersonPitchClamp;
        pitch = Math.clamp(pitch, -pitchClamp, pitchClamp);
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);

        float scale = AnimationUtil.calculateAnimatedScale(config.scale(), screenOpenTime, isRecipeBookOpen, recipeBookCloseDelay);
        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);
        matrices.scale(scale, scale, scale);

        Matrix4f pose = matrices.last().pose();

        BufferBuilder buffer = new BufferBuilder(INVENTORY_BYTE_BUFFER, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float aspect = (float) targetManager.getInventoryTarget().width / (float) targetManager.getInventoryTarget().height;
        RenderUtil.addScreenQuad(buffer, pose, aspect);

        MeshData meshData = buffer.build();

        WORLD_POSE_STACK.setIdentity();
        RenderUtil.applyScreenTransform(WORLD_POSE_STACK, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        WORLD_POSE_STACK.scale(scale, scale, scale);
        Matrix4f worldPose = WORLD_POSE_STACK.last().pose();

        quadBasis = RenderUtil.computeQuadBasis(worldPose, aspect, scale);

        matrices.popPose();

        if (meshData != null) {
            try {
                drawInventory(meshData, texture);
            } finally {
                meshData.close();
            }
        }
    }

    private void drawInventory(MeshData meshData, GpuTextureView texture) {
        Minecraft client = Minecraft.getInstance();
        RenderTarget mainTarget = client.getMainRenderTarget();
        var output = mainTarget.getColorTextureView();
        if (output == null) {
            return;
        }

        float fadeAlpha = SpatialGUI.config.enableFadeAnimation
                ? Math.min(1.0F, (System.currentTimeMillis() - screenOpenTime) / (float) SpatialGUI.config.fadeDurationMs)
                : 1.0F;
        COLOR_MODULATOR.set(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
                RenderSystem.getModelViewMatrix(),
                COLOR_MODULATOR,
                ZERO_VECTOR,
                IDENTITY_MATRIX
        );

        FilterMode filterMode = SpatialGUI.config.useLinearFiltering ? FilterMode.LINEAR : FilterMode.NEAREST;

        var sequentialBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = sequentialBuffer.getBuffer(6);
        VertexFormat.IndexType indexType = sequentialBuffer.type();

        try (GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Spatial GUI Quad",
                GpuBuffer.USAGE_VERTEX,
                meshData.vertexBuffer()
        )) {
            try (var renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                    () -> "Spatial GUI",
                    output,
                    java.util.OptionalInt.empty()
            )) {
                renderPass.setPipeline(InventoryRenderer.INVENTORY_PIPELINE);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.bindTexture("Sampler0", texture, RenderSystem.getSamplerCache().getSampler(
                        AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE,
                        filterMode, filterMode, SpatialGUI.config.useAnisotropicFiltering
                ));
                renderPass.setVertexBuffer(0, vertexBuffer);
                renderPass.setIndexBuffer(indexBuffer, indexType);
                renderPass.drawIndexed(0, 0, 6, 1);
            }
        }
    }
    *///?} else {
    public void renderInWorld(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();
        RenderTarget target = targetManager.getInventoryTarget();

        if (target == null || !SpatialGUIClient.renderer().shouldCapture() || client.player == null) {
            return;
        }

        int textureId = target.getColorTextureId();
        if (textureId <= 0) {
            return;
        }

        matrices.pushPose();

        var player = client.player;
        boolean isFirstPerson = SpatialGUIClient.getEffectiveFirstPersonMode();
        float yaw = player.getYRot();
        float pitch = isFirstPerson ? player.getXRot() : 0;
        float pitchClamp = (float) SpatialGUI.config.firstPersonPitchClamp;
        pitch = Math.clamp(pitch, -pitchClamp, pitchClamp);
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        RenderUtil.ScreenTransformConfig config = RenderUtil.getScreenTransformConfig(isFirstPerson);

        double lookX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double lookY = -Math.sin(pitchRadians);
        double lookZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        RenderUtil.applyScreenTransform(matrices, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);

        float scale = AnimationUtil.calculateAnimatedScale(config.scale(), screenOpenTime, isRecipeBookOpen, recipeBookCloseDelay);
        recipeBookCloseDelay = isRecipeBookOpen ? -2 : Math.min(0, recipeBookCloseDelay + 1);
        matrices.scale(scale, scale, scale);

        Matrix4f pose = matrices.last().pose();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float aspect = (float) target.width / (float) target.height;
        RenderUtil.addScreenQuad(buffer, pose, aspect);

        WORLD_POSE_STACK.setIdentity();
        RenderUtil.applyScreenTransform(WORLD_POSE_STACK, isFirstPerson, yawRadians, pitchRadians, config, lookX, lookY, lookZ);
        WORLD_POSE_STACK.scale(scale, scale, scale);
        Matrix4f worldPose = WORLD_POSE_STACK.last().pose();

        quadBasis = RenderUtil.computeQuadBasis(worldPose, aspect, scale);

        matrices.popPose();

        drawInventory(buffer.buildOrThrow(), textureId);
    }

    private void drawInventory(MeshData meshData, int textureId) {
        Minecraft client = Minecraft.getInstance();

        float fadeAlpha = SpatialGUI.config.enableFadeAnimation
                ? Math.min(1.0F, (System.currentTimeMillis() - screenOpenTime) / (float) SpatialGUI.config.fadeDurationMs)
                : 1.0F;

        int filter = SpatialGUI.config.useLinearFiltering ? GL11.GL_LINEAR : GL11.GL_NEAREST;

        client.getMainRenderTarget().bindWrite(true);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.bindTexture(textureId);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 33071); // CLAMP_TO_EDGE
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 33071);

        RenderSystem.setShaderColor(fadeAlpha, fadeAlpha, fadeAlpha, fadeAlpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            BufferUploader.drawWithShader(meshData);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
    //?}
}