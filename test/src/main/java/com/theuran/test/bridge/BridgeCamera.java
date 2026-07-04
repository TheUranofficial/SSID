package com.theuran.test.bridge;

import mchorse.bbs.bridge.IBridgeCamera;
import mchorse.bbs.camera.controller.CameraController;
import com.theuran.test.TestEngine;

public class BridgeCamera extends BaseBridge implements IBridgeCamera {
    public BridgeCamera(TestEngine engine) {
        super(engine);
    }

    @Override
    public CameraController getCameraController() {
        return this.engine.cameraController;
    }
}