package com.theuran.pokoyo;

import mchorse.bbs.core.Engine;

public class VulkanEngine extends Engine {
    public VulkanRenderer renderer;

    @Override
    public void init() throws Exception {
        super.init();

        this.renderer = new VulkanRenderer();
    }

    @Override
    public void render(float transition) {
        super.render(transition);

        this.renderer.render();
    }

    @Override
    public void delete() {
        super.delete();

        this.renderer.delete();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public boolean handleGamepad(int button, int action) {
        return false;
    }

    @Override
    public boolean handleKey(int key, int scancode, int action, int mods) {
        return false;
    }

    @Override
    public void handleTextInput(int key) {

    }

    @Override
    public void handleMouse(int button, int action, int mode) {

    }

    @Override
    public void handleScroll(double x, double y) {

    }
}
