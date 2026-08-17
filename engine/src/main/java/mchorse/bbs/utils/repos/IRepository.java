package mchorse.bbs.utils.repos;

import mchorse.bbs.data.IDataSerializable;
import mchorse.bbs.data.types.MapType;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

public interface IRepository<T extends IDataSerializable<?>> {
    default T create(String id) {
        return this.create(id, null);
    }

    T create(String id, MapType data);

    void load(String id, Consumer<T> callback);

    void save(String id, MapType data);

    void rename(String id, String name);

    void delete(String id);

    void requestKeys(Consumer<Collection<String>> callback);

    /* Folders */

    File getFolder();

    void addFolder(String path, Consumer<Boolean> callback);

    void renameFolder(String path, String name, Consumer<Boolean> callback);

    void deleteFolder(String path, Consumer<Boolean> callback);
}
