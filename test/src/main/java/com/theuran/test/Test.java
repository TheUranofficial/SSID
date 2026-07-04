package com.theuran.test;

import mchorse.bbs.BBS;
import mchorse.bbs.bridge.IBridgeWorld;
import mchorse.bbs.data.types.MapType;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.resources.Link;
import mchorse.bbs.utils.*;
import mchorse.bbs.utils.cli.ArgumentParser;
import mchorse.bbs.utils.cli.ArgumentType;
import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class Test {
    public static final String VERSION = "0.67";

    public static final Profiler PROFILER = new Profiler();

    /* Command line arguments */

    public File gameDirectory;
    public String defaultWorld = "flat";
    public int windowWidth = 1280;
    public int windowHeight = 720;
    public boolean openGLDebug;

    public static Link link(String path) {
        return new Link("test", path);
    }

    private static boolean canLock(File file) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            try (FileLock fileLock = randomAccessFile.getChannel().tryLock()) {
                if (fileLock != null) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            file.delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));

                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    static void main(String[] args) {
        PROFILER.begin("bootstrap");

        System.out.println(IOUtils.readText(BBS.class.getResourceAsStream("/assets/strings/title.txt")));
        System.out.println("\nBBS: " + VERSION + ", LWJGL: " + Version.getVersion() + ", GLFW: " + GLFW.glfwGetVersionString());

        System.setOut(new TimePrintStream(System.out));
        System.setErr(new TimePrintStream(System.err));

        ArgumentParser parser = new ArgumentParser();

        parser.register("gameDirectory", ArgumentType.PATH)
            .register("defaultWorld", "dw", ArgumentType.STRING)
            .register("glDebug", "gld", ArgumentType.NUMBER)
            .register("width", "ww", ArgumentType.NUMBER)
            .register("height", "wh", ArgumentType.NUMBER);

        Test game = new Test();

        game.setup(parser.parse(args));

        File lockFile = new File(game.gameDirectory, "instance.lock");

        if (canLock(lockFile)) {
            game.launch();
        } else {
            System.err.println("An instance of BBS Test is already running! Please shut it down.");
            System.err.println("If you're absolutely sure that it's not running, then remove " + lockFile.getAbsolutePath() + " file, and try launching again!");
            System.err.println("If you can't remove that file... then it's still running in the background!");

            JOptionPane.showMessageDialog(null, "BBS instance is already running!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setup(MapType data) {
        if (data.has("gameDirectory")) {
            this.gameDirectory = new File(data.getString("gameDirectory"));
        }

        this.defaultWorld = data.getString("defaultWorld", "flat");
        this.windowWidth = data.getInt("width", this.windowWidth);
        this.windowHeight = data.getInt("height", this.windowHeight);
        this.openGLDebug = data.getBool("glDebug", this.openGLDebug);
    }

    public void launch() {
        PROFILER.endBegin("launch");

        if (this.gameDirectory == null || !this.gameDirectory.isDirectory()) {
            throw new IllegalStateException("Given game directory '" + this.gameDirectory + "' doesn't exist or not a directory...");
        }

        TestEngine engine = new TestEngine(this);
        long id = -1;

        try {
            PROFILER.endBegin("setup_window");

            /* Start the game */
            Window.initialize("BBS " + VERSION, this.windowWidth, this.windowHeight, this.openGLDebug);
            Window.setupStates();

            id = Window.getWindow();

            PROFILER.endBegin("init_engine");
            engine.init();
            PROFILER.endBegin("load_world");
            engine.get(IBridgeWorld.class).loadWorld(this.defaultWorld);
            PROFILER.end();
            PROFILER.print();
            engine.start(id);
        } catch (Exception e) {
            File crashes = new File(this.gameDirectory, "crashes");
            Pair<File, String> crash = CrashReport.writeCrashReport(crashes, e, "BBS " + VERSION + " has crashed! Here is a crash stacktrace:");

            /* Here we should actually save a crash log with exception
             * and other relevant information */
            e.printStackTrace();

            CrashReport.showDialogue(crash, "BBS " + VERSION + " has crashed! The crash log " + crash.a.getName() + " was generated in \"crashes\" folder, which you should send to BBS' developer(s).\n\nIMPORTANT: don't screenshot this window!");
        }

        /* Terminate the game */
        engine.delete();

        Callbacks.glfwFreeCallbacks(id);
        GLFW.glfwDestroyWindow(id);

        GLFW.glfwSetErrorCallback(null).free();
        GLFW.glfwTerminate();
    }
}