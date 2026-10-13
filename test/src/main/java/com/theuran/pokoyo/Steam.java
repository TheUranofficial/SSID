package com.theuran.pokoyo;

import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.codedisaster.steamworks.SteamLibraryLoaderLwjgl3;
import org.lwjgl.system.Platform;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class Steam {
    private static final long CLIENT_START_TIMEOUT_NANOS = 60000000000L;

    public static void initialize() throws SteamException {
        if (!SteamAPI.loadLibraries(new SteamLibraryLoaderLwjgl3())) {
            throw new SteamException("Cannot find steam libraries");
        }

        SteamAPI.InitResult result = SteamAPI.initEx();

        if (result == SteamAPI.InitResult.NoSteamClient) {
            startClient();

            long startTime = System.nanoTime();

            while (result == SteamAPI.InitResult.NoSteamClient || result == SteamAPI.InitResult.FailedGeneric) {
                if (System.nanoTime() - startTime >= Steam.CLIENT_START_TIMEOUT_NANOS) {
                    throw new SteamException("Steam did not become ready within 60 seconds: " + result + ". Check Steam sign-in and the game App ID.");
                }

                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    throw new SteamException("Interrupted while waiting for Steam to start", e);
                }

                result = SteamAPI.initEx();
            }
        }

        if (result != SteamAPI.InitResult.OK) {
            throw new SteamException("Cannot initialize steam: " + result);
        }
    }

    private static void startClient() throws SteamException {
        IO.println("Starting Steam client...");

        try {
            if (Platform.get() == Platform.WINDOWS) {
                Process query = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Command",
                    "[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false); (Get-ItemProperty -LiteralPath 'HKCU:\\Software\\Valve\\Steam' -ErrorAction Stop).SteamExe"
                ).redirectErrorStream(true).start();
                String executable;

                try (InputStream output = query.getInputStream()) {
                    executable = new String(output.readAllBytes(), StandardCharsets.UTF_8).trim();
                }

                if (query.waitFor() != 0 || executable.isEmpty()) {
                    throw new SteamException("Cannot find the Steam installation in the Windows registry");
                }

                new ProcessBuilder(executable, "-silent").start();
            } else {
                if (!Desktop.isDesktopSupported()) {
                    throw new SteamException("Cannot start Steam automatically: desktop URI launching is unavailable");
                }

                Desktop desktop = Desktop.getDesktop();

                if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                    throw new SteamException("Cannot start Steam automatically: desktop URI launching is unsupported");
                }

                desktop.browse(URI.create("steam://open/main"));
            }
        } catch (IOException e) {
            throw new SteamException("Cannot start Steam client. Check that Steam is installed.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new SteamException("Interrupted while locating the Steam installation", e);
        }
    }

    public static void update() {
        if (SteamAPI.isSteamRunning()) {
            SteamAPI.runCallbacks();
        }
    }

    public static void delete() {
        if (SteamAPI.isSteamRunning()) {
            SteamAPI.shutdown();
        }
    }
}