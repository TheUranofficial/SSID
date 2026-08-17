package mchorse.bbs.data;

import mchorse.bbs.data.types.BaseType;

public interface IDataSerializable<T extends BaseType> {
    T toData();

    void fromData(T data);
}