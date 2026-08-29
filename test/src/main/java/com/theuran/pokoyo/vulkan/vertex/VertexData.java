package com.theuran.pokoyo.vulkan.vertex;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class VertexData {
    public ByteBuffer data;
    public final int vertexCount;

    public VertexData(ByteBuffer data, int vertexCount) {
        this.data = data;
        this.vertexCount = vertexCount;
    }

    public long sizeInBytes() {
        return (long) this.vertexCount * 36;
    }

    public void delete() {
        if (this.data != null) {
            MemoryUtil.memFree(this.data);
            this.data = null;
        }
    }
}