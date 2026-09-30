package org.tastytrash.spatialGUI.util;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;
import org.tastytrash.spatialGUI.SpatialGUI;
import org.tastytrash.spatialGUI.mixin.render.GameRendererInvoker;

public final class RenderUtil {
    private RenderUtil() {}

    public static void addQuadVertex(VertexConsumer buffer, Matrix4f pose,
            float x, float y, float z, float u, float v) {
        buffer.addVertex(pose, x, y, z).setUv(u, v).setColor(255, 255, 255, SpatialGUI.config.screenAlpha);
    }

    public static void applyScreenTransform(PoseStack matrices, boolean isFirstPerson,
            float yawRadians, float pitchRadians, ScreenTransformConfig config,
            double lookX, double lookY, double lookZ) {
        float yawOffsetRad = (float) Math.toRadians(config.yawOffset);
        float pitchOffsetRad = (float) Math.toRadians(config.pitchOffset);
        
        if (isFirstPerson) {
            matrices.translate(
                    lookX * config.distance + Math.cos(yawRadians) * config.sideOffset,
                    lookY * config.distance + config.heightOffset,
                    lookZ * config.distance + Math.sin(yawRadians) * config.sideOffset
            );
            matrices.mulPose(new Quaternionf()
                    .rotateY(-yawRadians + yawOffsetRad)
                    .rotateX(-pitchRadians + pitchOffsetRad)
                    .get(new Matrix4f())
            );
        } else {
            matrices.translate(
                    -Mth.sin(yawRadians) * config.distance + Math.cos(yawRadians) * config.sideOffset,
                    config.heightOffset,
                    Mth.cos(yawRadians) * config.distance + Math.sin(yawRadians) * config.sideOffset
            );
            matrices.mulPose(new Quaternionf()
                    .rotateY(-yawRadians + yawOffsetRad)
                    .rotateX(pitchOffsetRad)
                    .get(new Matrix4f())
            );
        }
    }

    public record ScreenTransformConfig(float distance, float sideOffset, float heightOffset, float yawOffset, float pitchOffset, float scale) { }

    private static float calculateFovScaleMultiplier(boolean autoScaleByFov) {
        if (!autoScaleByFov) return 1.0f;
        
        //? if >=26.1.2 {
        /*float currentFov = Minecraft.getInstance().gameRenderer.getMainCamera().getFov();
        *///?} else {
        float currentFov = (float) ((GameRendererInvoker) Minecraft.getInstance().gameRenderer)
                .spatialGUI$getFov(Minecraft.getInstance().gameRenderer.getMainCamera(),
                        Minecraft.getInstance().gameRenderer.getMainCamera().getPartialTickTime(), true);
        //?}
        float baselineFov = (float) SpatialGUI.config.autoFovTuning.autoScaleBaselineFov;
        float power = (float) SpatialGUI.config.autoFovTuning.autoScaleScreenPower;

        float ratio = currentFov / baselineFov;
        return (float) Math.pow(ratio, power);
    }

    public static ScreenTransformConfig getScreenTransformConfig(boolean isFirstPerson) {
        float fovMultiplier = calculateFovScaleMultiplier(SpatialGUI.config.autoScaleByFov);

        if (isFirstPerson) {
            return new ScreenTransformConfig(
                    (float) SpatialGUI.config.firstPersonScreenDistance,
                    (float) SpatialGUI.config.firstPersonScreenSideOffset,
                    (float) SpatialGUI.config.firstPersonScreenHeightOffset,
                    (float) SpatialGUI.config.firstPersonScreenYawOffset,
                    (float) SpatialGUI.config.firstPersonScreenPitchOffset,
                    (float) SpatialGUI.config.firstPersonScreenScale * fovMultiplier
            );
        }

        float thirdPersonFovMultiplier = 1.0f + (fovMultiplier - 1.0f) * (float) SpatialGUI.config.autoFovTuning.autoScaleThirdPersonScreenMultiplier;
        float sideOffset = (float) SpatialGUI.config.screenSideOffset;
        float yawOffset = (float) SpatialGUI.config.screenYawOffset;

        if (SpatialGUI.config.mirrorThirdPerson) {
            sideOffset = -sideOffset;
            yawOffset = -yawOffset;
        }

        return new ScreenTransformConfig(
                (float) SpatialGUI.config.screenDistance,
                sideOffset,
                (float) SpatialGUI.config.screenHeightOffset,
                yawOffset,
                (float) SpatialGUI.config.screenPitchOffset,
                (float) SpatialGUI.config.screenScale * thirdPersonFovMultiplier
        );
    }

    public static void addScreenQuad(VertexConsumer buffer, Matrix4f pose, float aspect) {
        float halfWidth = aspect * 0.5F;
        float halfHeight = 0.5F;
        addQuadVertex(buffer, pose, -halfWidth, -halfHeight, 0.0F, 0.0F, 0.0F);
        addQuadVertex(buffer, pose, halfWidth, -halfHeight, 0.0F, 1.0F, 0.0F);
        addQuadVertex(buffer, pose, halfWidth, halfHeight, 0.0F, 1.0F, 1.0F);
        addQuadVertex(buffer, pose, -halfWidth, halfHeight, 0.0F, 0.0F, 1.0F);
    }

    public record QuadBasis(Vector3f centerOffset, Vector3f right, Vector3f up, Vector3f normal, float halfWidth, float halfHeight) {}

    public static QuadBasis computeQuadBasis(Matrix4f worldPose, float aspect, float scale) {
        Vector3f centerOffset = worldPose.transformPosition(new Vector3f());
        Vector3f right = worldPose.transformDirection(1f, 0f, 0f, new Vector3f()).normalize();
        Vector3f up = worldPose.transformDirection(0f, 1f, 0f, new Vector3f()).normalize();
        Vector3f normal = new Vector3f(right).cross(up).normalize();
        float halfWidth = aspect * 0.5f * scale;
        float halfHeight = 0.5f * scale;
        return new QuadBasis(centerOffset, right, up, normal, halfWidth, halfHeight);
    }

    public static Vector2d getInventoryMousePositionRay(double screenX, double screenY, QuadBasis basis, TextureTarget inventoryTarget) {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        double ndcX = (screenX / width) * 2.0 - 1.0;
        double ndcY = 1.0 - (screenY / height) * 2.0;

        //? if >=26.2 {
        /*var camera = mc.gameRenderer.getMainCamera();
        float yawRadians = (float) Math.toRadians(camera.getYRot());
        float pitchRadians = (float) Math.toRadians(camera.getXRot());
        *///?} else {
        var camera = mc.gameRenderer.getMainCamera();
        float yawRadians = (float) Math.toRadians(camera.getYRot());
        float pitchRadians = (float) Math.toRadians(camera.getXRot());
        //?}

        float fx = (float) (-Math.sin(yawRadians) * Math.cos(pitchRadians));
        float fy = (float) (-Math.sin(pitchRadians));
        float fz = (float) (Math.cos(yawRadians) * Math.cos(pitchRadians));
        float fInv = 1.0f / (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        fx *= fInv;
        fy *= fInv;
        fz *= fInv;

        float rx = -fz;
        float ry = 0f;
        float rz = fx;
        float rInv = 1.0f / (float) Math.sqrt(rx * rx + rz * rz);
        rx *= rInv;
        rz *= rInv;

        float ux = ry * fz - rz * fy;
        float uy = rz * fx - rx * fz;
        float uz = rx * fy - ry * fx;
        float uInv = 1.0f / (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux *= uInv;
        uy *= uInv;
        uz *= uInv;

        //? if >=26.1.2 {
        /*float fovDegrees = camera.getFov();
         *///?} else {
        float fovDegrees = (float) ((GameRendererInvoker) mc.gameRenderer)
                .spatialGUI$getFov(camera, camera.getPartialTickTime(), true);
        //?}
        float aspect = (float) width / (float) height;
        float tanHalfFovY = (float) Math.tan(Math.toRadians(fovDegrees / 2.0));
        float tanHalfFovX = tanHalfFovY * aspect;

        float xCoeff = (float) ndcX * tanHalfFovX;
        float yCoeff = (float) ndcY * tanHalfFovY;

        float dx = fx + rx * xCoeff + ux * yCoeff;
        float dy = fy + ry * xCoeff + uy * yCoeff;
        float dz = fz + rz * xCoeff + uz * yCoeff;
        float dInv = 1.0f / (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        dx *= dInv;
        dy *= dInv;
        dz *= dInv;

        Vector3f norm = basis.normal();
        float denom = dx * norm.x() + dy * norm.y() + dz * norm.z();
        if (Math.abs(denom) < 1e-6f) {
            return null;
        }

        Vector3f center = basis.centerOffset();
        float t = (center.x() * norm.x() + center.y() * norm.y() + center.z() * norm.z()) / denom;
        if (t <= 0f) {
            return null;
        }

        float hx = dx * t - center.x();
        float hy = dy * t - center.y();
        float hz = dz * t - center.z();

        Vector3f bRight = basis.right();
        Vector3f bUp = basis.up();
        float localX = hx * bRight.x() + hy * bRight.y() + hz * bRight.z();
        float localY = hx * bUp.x() + hy * bUp.y() + hz * bUp.z();

        float u = (localX / basis.halfWidth() + 1f) / 2f;
        float v = (localY / basis.halfHeight() + 1f) / 2f;

        if (u < 0f || u > 1f || v < 0f || v > 1f) {
            return null;
        }

        return new Vector2d(u * width, (1.0 - v) * height);
    }
}
