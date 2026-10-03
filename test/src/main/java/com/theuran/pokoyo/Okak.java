package com.theuran.pokoyo;

import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.utils.CrashReport;
import mchorse.bbs.utils.Pair;
import mchorse.bbs.utils.TimePrintStream;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class Okak {
    static void main() {
        System.setOut(new TimePrintStream(System.out));
        System.setErr(new TimePrintStream(System.err));

        VulkanEngine engine = new VulkanEngine();
        long id = 0;

        try {
            Window.initializeVulkan("Okak", 1280, 720);

            id = Window.getWindow();

            engine.init();
            engine.start(id);
        } catch (Exception e) {
            File crashes = new File("crashes");
            Pair<File, String> crash = CrashReport.writeCrashReport(crashes, e, "SSID has crashed! Here is a crash stacktrace:");

            e.printStackTrace();

            CrashReport.showDialogue(crash, "SSID has crashed! The crash log " + crash.a.getName() + " was generated in \"crashes\" folder, which you should send to BBS' developer(s).");
        }

        engine.delete();

        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);
        GLFW.glfwTerminate();
    }
}