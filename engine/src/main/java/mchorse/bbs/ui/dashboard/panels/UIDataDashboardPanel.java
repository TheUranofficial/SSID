package mchorse.bbs.ui.dashboard.panels;

import mchorse.bbs.BBSSettings;
import mchorse.bbs.game.utils.ContentType;
import mchorse.bbs.settings.values.core.ValueGroup;
import mchorse.bbs.ui.Keys;
import mchorse.bbs.ui.UIKeys;
import mchorse.bbs.ui.dashboard.UIDashboard;
import mchorse.bbs.ui.dashboard.panels.overlay.UICRUDOverlayPanel;
import mchorse.bbs.ui.dashboard.panels.overlay.UIDataOverlayPanel;
import mchorse.bbs.ui.framework.UIContext;
import mchorse.bbs.ui.framework.elements.UIElement;
import mchorse.bbs.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs.ui.utils.UIDataUtils;
import mchorse.bbs.ui.utils.icons.Icons;
import mchorse.bbs.utils.Timer;
import mchorse.bbs.utils.interps.Interpolations;

import java.util.Collection;

public abstract class UIDataDashboardPanel<T extends ValueGroup> extends UICRUDDashboardPanel {
    public UIIcon saveIcon;

    protected T data;
    protected boolean save;

    private boolean openedBefore;

    private Timer savingTimer = new Timer(0);

    public UIDataDashboardPanel(UIDashboard dashboard) {
        super(dashboard);

        this.saveIcon = new UIIcon(Icons.SAVED, (b) -> this.save());

        this.iconBar.add(this.saveIcon);

        /* A separate element is needed to make save keybind a more priority than other keybinds, because
         * the keybinds are processed afterwards. */
        UIElement savePlease = new UIElement().noCulling();

        savePlease.keys().register(Keys.SAVE, this.saveIcon::clickItself).active(() -> this.data != null);
        this.add(savePlease);
    }

    public T getData() {
        return this.data;
    }

    /**
     * Get the content type of this panel
     */
    public abstract ContentType getType();

    @Override
    protected UICRUDOverlayPanel createOverlayPanel() {
        return new UIDataOverlayPanel<>(this.getTitle(), this, this::pickData);
    }

    @Override
    public void pickData(String id) {
        this.save();
        this.requestData(id);
    }

    public void requestData(String id) {
        this.getType().getRepository().load(id, data -> this.fill((T) data));
    }

    /* Data population */

    public void fill(T data) {
        this.data = data;

        this.saveIcon.setEnabled(data != null);
        this.editor.setVisible(data != null);
        this.overlay.dupe.setEnabled(data != null);
        this.overlay.rename.setEnabled(data != null);
        this.overlay.remove.setEnabled(data != null);

        this.fillData(data);

        this.savingTimer.mark(BBSSettings.editorPeriodicSave.get());
    }

    protected abstract void fillData(T data);

    public void fillDefaultData(T data) {
    }

    public void fillNames(Collection<String> names) {
        String value = this.data == null ? null : this.data.getId();

        this.overlay.namesList.fill(names);
        this.overlay.namesList.setCurrentFile(value);
    }

    @Override
    public void resize() {
        super.resize();

        if (!this.openedBefore) {
            this.openOverlay.resize();

            this.openedBefore = true;
        }
    }

    @Override
    public void requestNames() {
        UIDataUtils.requestNames(this.getType(), this::fillNames);
    }

    @Override
    public void open() {
        super.open();

        int seconds = BBSSettings.editorPeriodicSave.get();

        if (seconds > 0) {
            this.savingTimer.mark(seconds * 1000L);
        }
    }

    @Override
    public void close() {
        super.close();

        this.save();
    }

    public void save() {
        if (!this.update && this.data != null && this.editor.isEnabled()) {
            this.forceSave();
        }
    }

    public void forceSave() {
        this.preSave();
        this.getType().getRepository().save(this.data.getId(), this.data.toData().asMap());
    }

    protected void preSave() {
    }

    @Override
    public void render(UIContext context) {
        if (this.data == null) {
            double ticks = context.getTickTransition() % 15D;
            double factor = Math.abs(ticks / 15D * 2 - 1F);

            int x = this.openOverlay.area.x - 10 + (int) Interpolations.SINE_INOUT.interpolate(-10, 0, factor);
            int y = this.openOverlay.area.my();

            context.batcher.icon(Icons.ARROW_RIGHT, x, y, 0.5F, 0.5F);
        }

        super.render(context);

        if (!this.editor.isEnabled() && this.data != null) {
            this.renderLockedArea(context);
        }

        this.checkPeriodicSave(context);
    }

    private void checkPeriodicSave(UIContext context) {
        if (this.data == null) {
            return;
        }

        int seconds = BBSSettings.editorPeriodicSave.get();

        if (seconds > 0) {
            if (this.savingTimer.check()) {
                this.savingTimer.mark(seconds * 1000L);

                this.save();
                context.notifySuccess(UIKeys.PANELS_SAVED_NOTIFICATION.format(this.data.getId()));
            }
        }
    }
}