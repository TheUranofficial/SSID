package com.theuran.test.bridge;

import mchorse.bbs.bridge.IBridgeVideoScreenshot;
import mchorse.bbs.utils.recording.ScreenshotRecorder;
import mchorse.bbs.utils.recording.VideoRecorder;
import com.theuran.test.TestEngine;

public class BridgeVideoRecorder extends BaseBridge implements IBridgeVideoScreenshot {
    public BridgeVideoRecorder(TestEngine engine) {
        super(engine);
    }

    @Override
    public ScreenshotRecorder getScreenshotRecorder() {
        return this.engine.screenshot;
    }

    @Override
    public VideoRecorder getVideoRecorder() {
        return this.engine.video;
    }
}