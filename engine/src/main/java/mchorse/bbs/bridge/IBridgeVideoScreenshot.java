package mchorse.bbs.bridge;

import mchorse.bbs.utils.recording.ScreenshotRecorder;
import mchorse.bbs.utils.recording.VideoRecorder;

public interface IBridgeVideoScreenshot {
    ScreenshotRecorder getScreenshotRecorder();

    VideoRecorder getVideoRecorder();
}