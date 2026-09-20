package com.theuran.pokoyo.vulkan.vertex;

import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.allocation.Buffer;
import com.theuran.pokoyo.vulkan.allocation.UploadContext;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import mchorse.bbs.graphics.vao.VAO;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class BufferBuilder {
    private ByteBuffer buffer = VAO.DATA;

    public Buffer upload(UploadContext context, Allocator allocator, CommandBuffer commandBuffer) {
        this.buffer.flip();

        Buffer buffer = context.uploadVertexBuffer(this.buffer, allocator, commandBuffer);

        this.buffer.clear();

        return buffer;
    }

    public BufferBuilder begin() {
        this.buffer.clear();

        return this;
    }

    public BufferBuilder pos(float x, float y, float z) {
        this.buffer.putFloat(x);
        this.buffer.putFloat(y);
        this.buffer.putFloat(z);

        return this;
    }

    public BufferBuilder color(float r, float g, float b, float a) {
        this.buffer.put((byte) (r * 255));
        this.buffer.put((byte) (g * 255));
        this.buffer.put((byte) (b * 255));
        this.buffer.put((byte) (a * 255));

        return this;
    }

    public BufferBuilder tex(float u, float v) {
        this.buffer.putFloat(u);
        this.buffer.putFloat(v);

        return this;
    }

    public BufferBuilder tex(float u, float v, float textureWidth, float textureHeight) {
        return this.tex(u / textureWidth, v / textureHeight);
    }

    public void free() {
        MemoryUtil.memFree(this.buffer);
        this.buffer = null;
    }
}