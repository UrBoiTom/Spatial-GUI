package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.joml.Vector2d;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.mixin.gui.MouseHandlerAccessor;
import org.tastytrash.spatialGUI.util.RenderUtil.QuadBasis;

public class MouseHandlerUtil {
    private static boolean weGrabbedMouse = false;
    private static double lastPosX = Double.NaN;
    private static double lastPosY = Double.NaN;
    private static double cachedSrcX = Double.NaN;
    private static double cachedSrcY = Double.NaN;
    private static QuadBasis cachedQuadBasis = null;
    private static Vector2d cachedMouse = null;
    private static double cachedGuiScale = Double.NaN;

    public static void resetMouseCache() {
        lastPosX = Double.NaN;
        lastPosY = Double.NaN;
        cachedSrcX = Double.NaN;
        cachedSrcY = Double.NaN;
        cachedQuadBasis = null;
        cachedMouse = null;
        cachedGuiScale = Double.NaN;
    }

    public static Vector2d getOrComputeMousePosition(double srcX, double srcY, QuadBasis quadBasis, double guiScale, com.mojang.blaze3d.pipeline.TextureTarget target) {
        if (srcX == cachedSrcX && srcY == cachedSrcY && quadBasis == cachedQuadBasis && guiScale == cachedGuiScale) {
            return cachedMouse;
        }
        Vector2d mouse = RenderUtil.getInventoryMousePositionRay(srcX, srcY, quadBasis, target);
        cachedSrcX = srcX;
        cachedSrcY = srcY;
        cachedQuadBasis = quadBasis;
        cachedGuiScale = guiScale;
        cachedMouse = mouse;
        if (mouse != null) {
            lastPosX = mouse.x / guiScale;
            lastPosY = mouse.y / guiScale;
        } else {
            lastPosX = -2000.0;
            lastPosY = -2000.0;
        }
        return mouse;
    }

    public static double getLastPos(boolean isX, double fallback) {
        double val = isX ? lastPosX : lastPosY;
        return Double.isNaN(val) ? fallback : val;
    }
    //? if >26.2 || <1.21.11{
    private static double freeLookDeltaX = 0;
    private static double freeLookDeltaY = 0;

    public static void addFreeLookDelta(double xrel, double yrel) {
        freeLookDeltaX += xrel;
        freeLookDeltaY += yrel;
    }

    public static double[] resetFreeLookDelta() {
        double[] result = {freeLookDeltaX, freeLookDeltaY};
        freeLookDeltaX = 0;
        freeLookDeltaY = 0;
        return result;
    }
    //? }

    public static void grabMouseForFirstPerson() {
        Minecraft mc = Minecraft.getInstance();
        MouseHandlerAccessor accessor = (MouseHandlerAccessor) mc.mouseHandler;
        if (!accessor.getMouseGrabbed()) {
            accessor.setMouseGrabbed(true);
            double centerX = mc.getWindow().getScreenWidth() / 2.0;
            double centerY = mc.getWindow().getScreenHeight() / 2.0;
            //? if >26.2 {
            /*InputConstants.grabMouse(mc.getWindow(), centerX, centerY);
            *///?} else {
             InputConstants.grabOrReleaseMouse(mc.getWindow().getWindow(), InputConstants.CURSOR_DISABLED, centerX, centerY);
            //?}
            mc.mouseHandler.setIgnoreFirstMove();
        }
        weGrabbedMouse = true;
    }

    public static void releaseMouseFromFirstPerson() {
        if (!weGrabbedMouse) return;
        Minecraft mc = Minecraft.getInstance();
        MouseHandlerAccessor accessor = (MouseHandlerAccessor) mc.mouseHandler;
        if (accessor.getMouseGrabbed()) {
            accessor.setMouseGrabbed(false);
            double centerX = mc.getWindow().getScreenWidth() / 2.0;
            double centerY = mc.getWindow().getScreenHeight() / 2.0;
            //? if >26.2 {
            /*InputConstants.releaseMouse(mc.getWindow(), centerX, centerY);
            *///?} else {
             InputConstants.grabOrReleaseMouse(mc.getWindow().getWindow(), InputConstants.CURSOR_NORMAL, centerX, centerY);
            //?}
        }
        weGrabbedMouse = false;
    }

    public static void updateMouseGrabForFirstPerson(boolean isFirstPerson) {
        if (!SpatialGUI.config.useCrosshairForFirstPerson || !isFirstPerson) {
            releaseMouseFromFirstPerson();
        } else {
            grabMouseForFirstPerson();
        }
    }
}
