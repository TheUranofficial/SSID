package com.theuran.pokoyo.vulkan.shader;

public class PushConstantRange {
    public int stage;
    public int offset;
    public int size;

    public PushConstantRange(int stage, int offset, int size) {
        this.stage = stage;
        this.offset = offset;
        this.size = size;
    }
}
