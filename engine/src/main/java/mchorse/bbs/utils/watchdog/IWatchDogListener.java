package mchorse.bbs.utils.watchdog;

import java.nio.file.Path;

public interface IWatchDogListener {
    void accept(Path path, WatchDogEvent event);
}