package mchorse.bbs.data.storage;

import java.io.*;

public class DataFileStorage extends DataStorage {
    protected File file;

    public DataFileStorage(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return new FileInputStream(this.file);
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return new FileOutputStream(this.file);
    }
}