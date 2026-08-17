package mchorse.bbs.bridge;

import mchorse.bbs.ui.framework.UIBaseMenu;

public interface IBridgeMenu {
    UIBaseMenu getCurrentMenu();

    default void closeMenu() {
        this.showMenu(null);
    }

    void showMenu(UIBaseMenu menu);
}