package mchorse.bbs;

import mchorse.bbs.bridge.IBridge;
import mchorse.bbs.particles.ParticleManager;

import java.io.File;

public class BBSData {
    private static ParticleManager particles;

    public static ParticleManager getParticles() {
        return particles;
    }

    public static void load(File folder, IBridge bridge) {
        particles = new ParticleManager(() -> new File(folder, "particles"));
    }

    public static void delete() {
        particles = null;
    }
}