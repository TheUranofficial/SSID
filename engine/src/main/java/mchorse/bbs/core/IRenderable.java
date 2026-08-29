package mchorse.bbs.core;

/**
 * Renderable interface implementation can render some things
 */
public interface IRenderable {
    /**
     * When the window is getting resized, this renderer would get
     * called
     */
    void resize(int width, int height);

    /**
     * Render whatever this renderer is
     */
    void render(float transition);
}