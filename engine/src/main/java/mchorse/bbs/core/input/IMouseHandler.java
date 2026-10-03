package mchorse.bbs.core.input;

/**
 * Mouse handler interface
 */
public interface IMouseHandler {
    void handleMouse(int button, int action, int mode);

    void handleScroll(double x, double y);
}