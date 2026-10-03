package mchorse.bbs.world.entities.architect.blueprints;

import mchorse.bbs.world.entities.Entity;
import mchorse.bbs.world.entities.EntityRecord;

import java.util.List;

public interface IEntityBlueprint {
    void fillComponents(List<EntityRecord> records);

    void setupEntity(Entity entity);
}