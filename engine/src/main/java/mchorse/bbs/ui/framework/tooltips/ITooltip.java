package mchorse.bbs.ui.framework.tooltips;

import mchorse.bbs.l10n.keys.IKey;
import mchorse.bbs.ui.framework.UIContext;

public interface ITooltip {
    public IKey getLabel();

    public void renderTooltip(UIContext context);
}
