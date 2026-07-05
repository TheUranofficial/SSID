package mchorse.bbs.camera.controller;

import mchorse.bbs.camera.Camera;

public interface ICameraController {
    void setup(Camera camera, float transition);

    /**
     * Get camera controller priority. The camera controller with the highest
     * priority will get picked.
     */
    default int getPriority() {
        return 0;
    }
}