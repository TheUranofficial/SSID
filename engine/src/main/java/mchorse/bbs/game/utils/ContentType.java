package mchorse.bbs.game.utils;

import mchorse.bbs.BBSData;
import mchorse.bbs.settings.values.core.ValueGroup;
import mchorse.bbs.ui.dashboard.UIDashboard;
import mchorse.bbs.ui.dashboard.panels.UIDataDashboardPanel;
import mchorse.bbs.ui.particles.UIParticleSchemePanel;
import mchorse.bbs.utils.repos.FolderManagerRepository;
import mchorse.bbs.utils.repos.IRepository;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContentType {
    public static final ContentType PARTICLES = new ContentType("particles", () -> new FolderManagerRepository<>(BBSData.getParticles()), dashboard -> dashboard.getPanel(UIParticleSchemePanel.class));

    private final String id;
    private Supplier<IRepository<? extends ValueGroup>> repository;
    private Function<UIDashboard, UIDataDashboardPanel<?>> dashboardPanel;

    public ContentType(String id, Supplier<IRepository<? extends ValueGroup>> repository, Function<UIDashboard, UIDataDashboardPanel<?>> dashboardPanel) {
        this.id = id;
        this.repository = repository;
        this.dashboardPanel = dashboardPanel;
    }

    public String getId() {
        return this.id;
    }

    public IRepository<? extends ValueGroup> getRepository() {
        return this.repository.get();
    }

    public UIDataDashboardPanel<?> get(UIDashboard dashboard) {
        return this.dashboardPanel.apply(dashboard);
    }
}