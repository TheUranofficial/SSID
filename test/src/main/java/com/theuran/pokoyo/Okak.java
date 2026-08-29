package com.theuran.pokoyo;

import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.utils.TimePrintStream;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

public class Okak {
    static void main() {
        System.setOut(new TimePrintStream(System.out));
        System.setErr(new TimePrintStream(System.err));

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