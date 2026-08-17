package mchorse.bbs.world.entities.components;

import mchorse.bbs.graphics.RenderingContext;

public interface IRenderableComponent {
    void render(RenderingContext context);

    default int getRenderPriority() {
        return 1;
    }
}