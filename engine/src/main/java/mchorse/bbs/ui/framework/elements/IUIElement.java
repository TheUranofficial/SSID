package mchorse.bbs.ui.framework.elements;

import mchorse.bbs.ui.framework.UIContext;
import mchorse.bbs.ui.utils.Area;

public interface IUIElement {
    /**
     * Should be called when position has to be recalculated
     */
    void resize();

    /**
     * Whether this element is enabled (and can accept any input)
     */
    boolean isEnabled();

    /**
     * Whether this element is visible
     */
    boolean isVisible();

    /**
     * Mouse was clicked
     */
    boolean mouseClicked(UIContext context);

    /**
     * Mouse wheel was scrolled
     */
    boolean mouseScrolled(UIContext context);

    /**
     * Mouse was released
     */
    boolean mouseReleased(UIContext context);

    /**
     * Key was typed
     */
    boolean keyPressed(UIContext context);

    /**
     * Text was inputted
     */
    boolean textInput(UIContext context);

    /**
     * Determines whether this element can be rendered on the screen
     */
    boolean canBeRendered(Area viewport);

    /**
     * Draw its components on the screen
     */
    void render(UIContext context);
}