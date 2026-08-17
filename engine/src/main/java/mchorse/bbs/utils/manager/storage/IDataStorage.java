package mchorse.bbs.utils.manager.storage;

import mchorse.bbs.data.types.MapType;

import java.io.File;
import java.io.IOException;

public interface IDataStorage {
    MapType load(File file) throws IOException;

    void save(File file, MapType data) throws IOException;
}