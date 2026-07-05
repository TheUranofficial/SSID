package mchorse.bbs.utils.manager;

import mchorse.bbs.data.types.MapType;
import mchorse.bbs.settings.values.ValueGroup;
import mchorse.bbs.utils.StringUtils;
import mchorse.bbs.utils.manager.storage.IDataStorage;
import mchorse.bbs.utils.manager.storage.JSONLikeStorage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

/**
 * Base JSON manager which loads and saves different data
 * structures based upon Data API
 */
public abstract class BaseManager<T extends ValueGroup> extends FolderManager<T> {
    protected IDataStorage storage = new JSONLikeStorage();
    protected boolean backUps;

    public BaseManager(Supplier<File> folder) {
        super(folder);
    }

    @Override
    public final T create(String id, MapType data) {
        T object = this.createData(id, data);

        object.setId(id);

        return object;
    }

    protected abstract T createData(String id, MapType mapType);

    @Override
    public T load(String id) {
        try {
            MapType mapType = this.storage.load(this.getFile(id));

            return this.create(id, mapType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean save(T data) {
        return this.save(data.getId(), data.toData().asMap());
    }

    @Override
    public boolean save(String id, MapType data) {
        File file = this.getFile(id);

        try {
            if (this.backUps) {
                String path = file.getParentFile().getAbsolutePath();
                String backupFileName = new SimpleDateFormat("yyyy_MM_dd_HH").format(new Date());
                String filename = StringUtils.fileName(id);
                File backupFile = new File(path, "_" + filename + "/" + filename + "." + backupFileName + this.getExtension());

                backupFile.getParentFile().mkdirs();

                if (file.exists()) {
                    Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.storage.save(file, data);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}