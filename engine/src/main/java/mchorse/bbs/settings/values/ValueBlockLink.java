package mchorse.bbs.settings.values;

import mchorse.bbs.data.types.BaseType;
import mchorse.bbs.data.types.StringType;
import mchorse.bbs.settings.values.base.BaseValueBasic;
import mchorse.bbs.voxel.blocks.BlockLink;
import mchorse.bbs.voxel.blocks.IBlockVariant;
import mchorse.bbs.voxel.tilesets.BlockSet;

public class ValueBlockLink extends BaseValueBasic<BlockLink> {
    public ValueBlockLink(String id) {
        super(id, null);
    }

    public IBlockVariant get(BlockSet set) {
        return set.getVariant(this.value);
    }

    @Override
    public BaseType toData() {
        return new StringType(this.value == null ? "" : this.value.toString());
    }

    @Override
    public void fromData(BaseType data) {
        if (data.isString()) {
            this.value = BlockLink.create(data.asString());
        }
    }
}