package com.theuran.pokoyo;

import mchorse.bbs.camera.controller.CameraController;
import mchorse.bbs.core.Engine;
import mchorse.bbs.graphics.window.Window;

public class VulkanEngine extends Engine {
    public CameraController cameraController = new CameraController();

    public VulkanRenderer renderer;

    @Override
    public void init() throws Exception {
        super.init();

        this.renderer = new VulkanRenderer(this);
        this.cameraController.camera.position.set(0, 0.5, 0);

        Window.focus();
        Window.toggleMousePointer(true);
    }

    @Override
    public void render(float transition) {
        super.render(transition);

        this.cameraController.setup(this.cameraController.camera, transition);

        this.renderer.render();
    }

    @Override
    public void delete() {
        super.delete();

        this.renderer.delete();
    }

    @Override
    public void resize(int width, int height) {
        this.renderer.resize();
    }

    @Override
    public void update() {
        super.update();

        this.cameraController.tick();
    }

    @Override
    public boolean handleGamepad(int button, int action) {
        return false;
    }

    @Override
    public boolean handleKey(int key, int scancode, int action, int mods) {
        return this.keys.keybinds.handleKey(key, scancode, action, mods);
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
