package mchorse.bbs.settings.values.ui;

import mchorse.bbs.data.types.BaseType;
import mchorse.bbs.data.types.StringType;
import mchorse.bbs.settings.values.core.ValueString;

/**
 * Value language.
 *
 * <p>This value subclass stores language localization ID. IMPORTANT: the
 * language strings don't get reloaded automatically! You need to attach a
 * callback to the value.</p>
 */
public class ValueLanguage extends ValueString {
    public ValueLanguage(String id) {
        super(id, "");
    }

    @Override
    public void fromData(BaseType data) {
        if (BaseType.isString(data)) {
            data = new StringType(data.asString().toLowerCase());
        }

        super.fromData(data);
    }
}