package mchorse.bbs.bridge;

import mchorse.bbs.voxel.ChunkBuilder;
import mchorse.bbs.world.World;

public interface IBridgeWorld {
    World getWorld();

    default ChunkBuilder getChunkBuilder() {
        return this.getWorld().chunks.builder;
    }

    boolean loadWorld(String world);
}