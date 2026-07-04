package mchorse.studio.bridge;

import mchorse.bbs.bridge.IBridgeCamera;
import mchorse.bbs.camera.controller.CameraController;
import mchorse.studio.StudioEngine;

public class BridgeCamera extends BaseBridge implements IBridgeCamera {
    public BridgeCamera(StudioEngine engine) {
        super(engine);
    }

    @Override
    public CameraController getCameraController() {
        return this.engine.cameraController;
    }
}