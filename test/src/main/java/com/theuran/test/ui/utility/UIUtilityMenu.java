package com.theuran.test.ui.utility;

import mchorse.bbs.bridge.IBridge;
import mchorse.bbs.resources.Link;
import mchorse.bbs.ui.framework.UIBaseMenu;
import mchorse.bbs.ui.framework.elements.overlay.UIOverlay;
import com.theuran.test.Test;
import com.theuran.test.ui.UIKeysApp;

public class UIUtilityMenu extends UIBaseMenu {
    public UIUtilityMenu(IBridge bridge) {
        super(bridge);

        UIOverlay.addOverlay(this.context, new UIUtilityOverlayPanel(UIKeysApp.UTILITY_TITLE, this::closeThisMenu));
    }

    @Override
    public Link getMenuId() {
        return Test.link("utility");
    }

    @Override
    public boolean canPause() {
        return false;
    }
}