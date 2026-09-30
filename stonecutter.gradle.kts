plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.1-forge"

stonecutter parameters {
    constants.match(node.metadata.project.substringAfterLast('-'), "fabric", "neoforge")

    replacements {
        string(current.parsed < "26.2") {
            replace("gameRenderer.mainCamera()", "gameRenderer.getMainCamera()")
            replace("= client.gui.screen()", "= client.screen")
        }

        string(current.parsed < "1.21.11") {
            replace("camera.yRot()", "camera.getYRot()")
            replace("camera.xRot()", "camera.getXRot()")
            replace(".grabOrReleaseMouse(mc.getWindow(), ", ".grabOrReleaseMouse(mc.getWindow().getWindow(), ")
            replace("invertMouseY()", "invertYMouse()")
        }

        string(current.parsed < "1.21.11") {
            replace(
                "me.shedaniel.autoconfig.AutoConfigClient.getConfigScreen",
                "me.shedaniel.autoconfig.AutoConfig.getConfigScreen"
            )
        }
    }
}