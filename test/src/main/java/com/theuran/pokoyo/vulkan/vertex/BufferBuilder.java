package com.theuran.pokoyo.vulkan.vertex;

import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.allocation.Buffer;
import com.theuran.pokoyo.vulkan.allocation.UploadContext;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import mchorse.bbs.utils.MathUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferBuilder {
    private ByteBuffer buffer;
    private int vertexCapacity;
    private int vertexCount;
    private boolean building;
    private boolean free;

    private Vector3f pos;
    private Vector4f color;
    private Vector2f tex;
    private Vector3f normal;

    public BufferBuilder() {
        this(256);
    }

    public BufferBuilder(int capacity) {
        this.vertexCapacity = capacity;
        this.buffer = MemoryUtil.memAlloc(this.vertexCapacity * 12);
    }

    public Buffer upload(UploadContext context, Allocator allocator, CommandBuffer buffer) {
        return context.uploadVertexBuffer(this.buffer, allocator, buffer);
    }

    public BufferBuilder begin() {
        this.checkFree();

        if (this.building) {
            throw new IllegalStateException("Buffer Builder already in building statement");
        }

        this.building = true;
        this.vertexCount = 0;

        return this;
    }

    public BufferBuilder pos(float x, float y, float z) {
        this.pos = new Vector3f(x, y, z);

        return this;
    }

    public BufferBuilder color(float r, float g, float b, float a) {
        this.color = new Vector4f(r, g, b, a);

        return this;
    }

    public BufferBuilder tex(float u, float v) {
        this.tex = new Vector2f(u, v);

        return this;
    }

    public BufferBuilder normal(float x, float y, float z) {
        this.normal = new Vector3f(x, y, z);

        return this;
    }

    public BufferBuilder endVertex() {
        this.checkBuilding();
        this.computeCapacity(this.vertexCount + 1);

        int base = this.vertexCount * 12;

        this.buffer.putFloat(base, this.pos.x);
        this.buffer.putFloat(base + 4, this.pos.y);
        this.buffer.putFloat(base + 8, this.pos.z);

//        this.buffer.put(base + 12, this.compressColor(this.color.x));
//        this.buffer.put(base + 13, this.compressColor(this.color.y));
//        this.buffer.put(base + 14, this.compressColor(this.color.z));
//        this.buffer.put(base + 15, this.compressColor(this.color.w));
//
//        this.buffer.putFloat(base + 16, this.tex.x);
//        this.buffer.putFloat(base + 20, this.tex.y);
//
//        this.buffer.putFloat(base + 24, this.normal.x);
//        this.buffer.putFloat(base + 28, this.normal.y);
//        this.buffer.putFloat(base + 32, this.normal.z);

        this.vertexCount++;

        return this;
    }

    public void free() {
        if (!this.free) {
            MemoryUtil.memFree(this.buffer);
            this.buffer = null;
            this.free = true;
        }
    }

    private void computeCapacity(int vertexCount) {
        if (vertexCount <= this.vertexCapacity) {
            return;
        }

        int capacity = this.vertexCapacity;

        while (capacity < vertexCount) {
            capacity *= 2;
        }

        this.buffer = MemoryUtil.memRealloc(this.buffer, capacity * 12);
        this.vertexCapacity = capacity;
    }

    private void checkBuilding() {
        this.checkFree();

        if (!this.building) {
            throw new IllegalStateException("Buffer Builder is not in building statement");
        }
    }

    private void checkFree() {
        if (this.free) {
            throw new IllegalStateException("Buffer Builder is already free");
        }
    }

    private byte compressColor(float color) {
        float clamp = MathUtils.clamp(0, color, 1);

        return (byte) Math.round(clamp * 255);
    }
}