package com.theuran.test.bridge;

import mchorse.bbs.bridge.IBridgeMenu;
import mchorse.bbs.ui.framework.UIBaseMenu;
import com.theuran.test.TestEngine;

public class BridgeMenu extends BaseBridge implements IBridgeMenu {
    public BridgeMenu(TestEngine engine) {
        super(engine);
    }

    @Override
    public UIBaseMenu getCurrentMenu() {
        return this.engine.screen.menu;
    }

    @Override
    public void showMenu(UIBaseMenu menu) {
        this.engine.screen.showMenu(menu);
    }
}