package mchorse.bbs.world;

import mchorse.bbs.utils.AABB;

public interface IWorldObject {
    AABB getPickingHitbox();
}