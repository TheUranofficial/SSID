package mchorse.bbs.utils.manager;

import mchorse.bbs.data.IDataSerializable;
import mchorse.bbs.data.types.MapType;

import java.util.Collection;

public interface IManager<T extends IDataSerializable<?>> {
    boolean exists(String name);

    default T create(String id) {
        return this.create(id, null);
    }

    T create(String id, MapType data);

    T load(String id);

    boolean save(String name, MapType mapType);

    boolean rename(String from, String to);

    boolean delete(String name);

    Collection<String> getKeys();
}