package mchorse.bbs.ui.framework.elements.input;

import mchorse.bbs.BBSSettings;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.ui.UIKeys;
import mchorse.bbs.ui.framework.UIContext;
import mchorse.bbs.ui.framework.elements.UIElement;
import mchorse.bbs.ui.utils.keys.KeyAction;
import mchorse.bbs.ui.utils.keys.KeyCombo;
import mchorse.bbs.utils.colors.Colors;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class UIKeybind extends UIElement {
    public KeyCombo combo;
    public boolean reading;
    public Consumer<KeyCombo> callback;

    private boolean mouse;
    private boolean escape;

    private boolean first;

    public UIKeybind(Consumer<KeyCombo> callback) {
        super();

        this.combo = new KeyCombo(null, 0);
        this.combo.keys.clear();

        this.callback = callback;
        this.h(20);
    }

    public UIKeybind mouse() {
        this.mouse = true;

        return this;
    }

    public UIKeybind escape() {
        this.escape = true;

        return this;
    }

    public void setKeyCodes(int... keys) {
        this.combo.keys.clear();

        for (int i : keys) {
            this.combo.keys.add(i);
        }
    }

    public void setKeyCombo(KeyCombo combo) {
        this.combo.copy(combo);
    }

    @Override
    public boolean subMouseClicked(UIContext context) {
        if (this.area.isInside(context) && context.mouseButton == 0) {
            context.unfocus();

            this.first = true;
            this.reading = true;
            this.combo.keys.clear();
        } else if (this.reading && this.mouse) {
            int key = -context.mouseButton;

            if (!this.combo.keys.contains(key)) {
                this.combo.keys.addFirst(key);
            }

            return true;
        }

        return this.area.isInside(context);
    }

    @Override
    protected boolean subMouseReleased(UIContext context) {
        if (this.first) {
            this.first = false;
        } else if (this.reading && this.mouse) {
            this.finish();

            return true;
        }

        return super.subMouseReleased(context);
    }

    @Override
    public boolean subKeyPressed(UIContext context) {
        if (this.reading) {
            if (!this.escape && context.isPressed(GLFW.GLFW_KEY_ESCAPE)) {
                this.combo.keys.clear();
                this.finish();

                return true;
            }

            if (context.getKeyAction() == KeyAction.PRESSED) {
                int key = context.getKeyCode();

                if (!this.combo.keys.contains(key)) {
                    this.combo.keys.addFirst(key);
                }
            }

            if (this.combo.keys.isEmpty()) {
                return false;
            }

            for (int key : this.combo.keys) {
                if (Window.isKeyPressed(key)) {
                    return true;
                }
            }

            this.finish();

            return true;
        }

        return super.subKeyPressed(context);
    }

    private void finish() {
        this.reading = false;

        this.callback();
    }

    private void callback() {
        if (this.callback != null) {
            this.callback.accept(this.combo);
        }
    }

    @Override
    public void render(UIContext context) {
        String label = this.combo.keys.isEmpty() ? UIKeys.GENERAL_NONE.get() : this.combo.getKeyCombo();
        int w = context.font.getWidth(label) - 1;

        if (this.reading) {
            this.area.render(context.batcher, Colors.A100 | BBSSettings.primaryColor.get());

            int x = this.area.mx(w);
            int y = this.area.my() + context.font.getHeight() - 1;
            float a = (float) Math.sin(context.getTickTransition() / 2D);
            int c = Colors.setA(Colors.WHITE, a * 0.5F + 0.5F);

            context.batcher.box(x, y, x + w, y + 1, c);
        } else {
            this.area.render(context.batcher, Colors.A100);
        }

        context.batcher.textShadow(label, this.area.mx(w), this.area.my() - (float) context.font.getHeight() / 2);

        super.render(context);
    }
}