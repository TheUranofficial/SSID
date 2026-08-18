package mchorse.bbs.ui.world.tools.schematic;

import mchorse.bbs.voxel.Chunk;
import mchorse.bbs.voxel.blocks.IBlockVariant;
import mchorse.bbs.voxel.tilesets.BlockSet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Schematic class that is responsible for reading schematic data from NBT,
 * and replacing
 */
public class Schematic {
    private Chunk chunk;
    private List<Integer> data;

    public static int toIndex(int x, int y, int z, int w, int d) {
        return (y * d + z) * w + x;
    }

    public Schematic(Chunk chunk, List<Integer> data) {
        this.chunk = chunk;
        this.data = data;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public Set<Integer> getUniqueBlocks() {
        Set<Integer> unique = new LinkedHashSet<>();

        for (Integer integer : this.data) {
            if (integer != 0) {
                unique.add(integer);
            }
        }

        return unique;
    }

    public int getDataBlockAt(int x, int y, int z) {
        if (this.chunk.isOutside(x, y, z)) {
            return 0;
        }

        return this.data.get(toIndex(x, y, z, this.chunk.w, this.chunk.d));
    }

    public void replace(Integer pair, IBlockVariant b) {
        int w = this.chunk.w;
        int h = this.chunk.h;
        int d = this.chunk.d;

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    Integer block = this.data.get(toIndex(x, y, z, w, d));

                    if (block.equals(pair)) {
                        this.chunk.setBlock(x, y, z, b);
                    }
                }
            }
        }
    }
}