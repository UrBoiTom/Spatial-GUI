package org.tastytrash.spatialGUI.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;
import org.tastytrash.spatialGUI.util.RenderUtil;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

//? if >=26.1.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
*///?} else if >1.21.1 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.*;
import net.minecraft.client.gui.render.state.GuiRenderState;
import com.mojang.blaze3d.platform.Window;
*///?} else {
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
//?}

//? if neoforge && >=26.1.2 {
/*import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import net.minecraft.client.renderer.state.gui.pip.*;
*///?} else if neoforge && >1.21.1 {
/*import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import net.minecraft.client.gui.render.state.pip.*;
*///?}

import java.util.List;

public class ScreenExtractor {
    //? if >1.21.1 {
    /*private GuiRenderState screenRenderState;
    private GuiRenderer screenGuiRenderer;
    *///?}

    public void ensureScreenGuiRenderer() {
        //? if >1.21.1 {
        /*if (screenGuiRenderer == null) {
            Minecraft mc = Minecraft.getInstance();
            screenRenderState = new GuiRenderState();
            //? if fabric && >=26.2 {
            /^screenGuiRenderer = new GuiRenderer(
                    screenRenderState,
                    mc.gameRenderer.featureRenderDispatcher(),
                    List.of(
                            new GuiEntityRenderer(mc.getEntityRenderDispatcher()),
                            new GuiSkinRenderer(),
                            new GuiBookModelRenderer(),
                            new GuiBannerResultRenderer(mc.getAtlasManager()),
                            new GuiProfilerChartRenderer()
                    )
            );
            ^///?} else if neoforge && >=26.2 {
            /^screenGuiRenderer = new GuiRenderer(
                    screenRenderState,
                    mc.gameRenderer.featureRenderDispatcher(),
                    List.of(
                            new PictureInPictureRendererRegistration<>(GuiEntityRenderState.class, () -> new GuiEntityRenderer(mc.getEntityRenderDispatcher())),
                            new PictureInPictureRendererRegistration<>(GuiSkinRenderState.class, GuiSkinRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBookModelRenderState.class, GuiBookModelRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBannerResultRenderState.class, () -> new GuiBannerResultRenderer(mc.getAtlasManager())),
                            new PictureInPictureRendererRegistration<>(GuiProfilerChartRenderState.class, GuiProfilerChartRenderer::new)
                    )
            );
            ^///?} else if fabric {
            /^screenGuiRenderer = new GuiRenderer(
                    screenRenderState,
                    mc.renderBuffers().bufferSource(),
                    mc.gameRenderer.getSubmitNodeStorage(),
                    mc.gameRenderer.getFeatureRenderDispatcher(),
                    List.of(
                            new GuiEntityRenderer(mc.renderBuffers().bufferSource(), mc.getEntityRenderDispatcher()),
                            new GuiSkinRenderer(mc.renderBuffers().bufferSource()),
                            new GuiBookModelRenderer(mc.renderBuffers().bufferSource()),
                            new GuiBannerResultRenderer(mc.renderBuffers().bufferSource(), mc.getAtlasManager()),
                            new GuiSignRenderer(mc.renderBuffers().bufferSource(), mc.getAtlasManager()),
                            new GuiProfilerChartRenderer(mc.renderBuffers().bufferSource())
                    )
            );
            ^///?} else if neoforge {
            /^screenGuiRenderer = new GuiRenderer(
                    screenRenderState,
                    mc.renderBuffers().bufferSource(),
                    mc.gameRenderer.getSubmitNodeStorage(),
                    mc.gameRenderer.getFeatureRenderDispatcher(),
                    List.of(
                            new PictureInPictureRendererRegistration<>(GuiEntityRenderState.class, bufferSource -> new GuiEntityRenderer(bufferSource, mc.getEntityRenderDispatcher())),
                            new PictureInPictureRendererRegistration<>(GuiSkinRenderState.class, GuiSkinRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBookModelRenderState.class, GuiBookModelRenderer::new),
                            new PictureInPictureRendererRegistration<>(GuiBannerResultRenderState.class, bufferSource -> new GuiBannerResultRenderer(bufferSource, mc.getAtlasManager())),
                            new PictureInPictureRendererRegistration<>(GuiProfilerChartRenderState.class, GuiProfilerChartRenderer::new)
                    )
            );
            ^///?}
        }
        *///?}
    }

    //? if >1.21.1 {
    /*public GuiRenderer getScreenGuiRenderer() {
        ensureScreenGuiRenderer();
        return screenGuiRenderer;
    }
    *///?}

    public void extractIsolatedScreen(Screen screen, float partialTick, QuadBasis quadBasis, TextureTargetManager targetManager) {
        ensureScreenGuiRenderer();
        Minecraft mc = Minecraft.getInstance();

        double srcX, srcY;
        if (SpatialGUIRenderer.isCrosshairModeActive()) {
            srcX = mc.getWindow().getScreenWidth() / 2.0;
            srcY = mc.getWindow().getScreenHeight() / 2.0;
        } else {
            srcX = ((MouseHandlerAccessor) mc.mouseHandler).getRawXpos();
            srcY = ((MouseHandlerAccessor) mc.mouseHandler).getRawYpos();
        }

        Vector2d mapped = quadBasis != null
                ? RenderUtil.getInventoryMousePositionRay(srcX, srcY, quadBasis, targetManager.getInventoryTarget())
                : null;

        int mouseX, mouseY;
        if (mapped != null) {
            double guiScale = SpatialGUI.config.getEffectiveGuiScale(mc.getWindow().getHeight());
            mouseX = (int) (mapped.x / guiScale);
            mouseY = (int) (mapped.y / guiScale);
        } else {
            mouseX = -2000;
            mouseY = -2000;
        }

        SpatialGUIRenderer.isExtractingScreen = true;
        //? if >=26.1.2 {
        /*GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(mc, screenRenderState, mouseX, mouseY);
        screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        *///?} else if >1.21.1 {
        /*GuiGraphics graphics = new GuiGraphics(mc, screenRenderState, mouseX, mouseY);
        screen.renderWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
        *///?} else {
        var target = targetManager.getTarget();
        if (target != null) {
            Window window = mc.getWindow();
            float guiWidth = (float) (window.getWidth() / window.getGuiScale());
            float guiHeight = (float) (window.getHeight() / window.getGuiScale());

            Matrix4f oldProjection = RenderSystem.getProjectionMatrix();
            VertexSorting oldSorting = RenderSystem.getVertexSorting();
            var modelView = RenderSystem.getModelViewStack();

            try {
                target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);

                RenderSystem.setProjectionMatrix(
                        new Matrix4f().setOrtho(0.0F, guiWidth, guiHeight, 0.0F, 1000.0F, 21000.0F),
                        VertexSorting.ORTHOGRAPHIC_Z
                );
                modelView.pushMatrix();
                modelView.translation(0.0F, 0.0F, -11000.0F);
                RenderSystem.applyModelViewMatrix();

                GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                screen.renderWithTooltip(graphics, mouseX, mouseY, partialTick);
                graphics.flush();
            } finally {
                modelView.popMatrix();
                RenderSystem.applyModelViewMatrix();
                RenderSystem.setProjectionMatrix(oldProjection, oldSorting);
                mc.getMainRenderTarget().bindWrite(true);
            }
        }
        //?}
        SpatialGUIRenderer.isExtractingScreen = false;
    }
}