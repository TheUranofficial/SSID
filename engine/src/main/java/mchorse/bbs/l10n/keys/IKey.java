package mchorse.bbs.l10n.keys;

import java.util.List;

public interface IKey {
    IKey EMPTY = new StringKey("");

    /**
     * This method is used to create an IKey that contains raw string data.
     */
    static IKey raw(String string) {
        return new StringKey(string);
    }

    static IKey constant(String string) {
        return new StringKey(string);
    }

    static IKey comp(List<IKey> keys) {
        return new CompoundKey(keys);
    }

    String get();

    default IKey format(Object... args) {
        return new FormatKey(this, args);
    }
}