package com.theuran.pokoyo;

import mchorse.bbs.graphics.window.Window;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

public class Okak {
    static void main() {
        VulkanEngine engine = new VulkanEngine();
        long id;

        try {
            Window.initializeVulkan("Okak", 1280, 720);

            id = Window.getWindow();

            engine.init();
            engine.start(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        engine.delete();

        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);
        GLFW.glfwTerminate();
    }
}