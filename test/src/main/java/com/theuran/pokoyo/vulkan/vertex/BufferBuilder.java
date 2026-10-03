package com.theuran.pokoyo.vulkan.vertex;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.allocation.Buffer;
import com.theuran.pokoyo.vulkan.allocation.UploadContext;
import mchorse.bbs.graphics.vao.VAO;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class BufferBuilder {
    private ByteBuffer buffer = VAO.DATA;
    private ShortBuffer indices;
    private int vertexCount;
    private int indexCount;
    private boolean indexed;

    public Buffer upload(VulkanContext context, UploadContext upload) {
        this.buffer.flip();

        Buffer buffer = upload.uploadVertexBuffer(context, this.buffer);

        this.buffer.clear();

        return buffer;
    }

    public Buffer uploadIndices(VulkanContext context, UploadContext upload) {
        this.indices.flip();

        ByteBuffer view = MemoryUtil.memByteBuffer(MemoryUtil.memAddress(this.indices), this.indices.remaining() * Short.BYTES);
        Buffer buffer = upload.uploadIndexBuffer(context, view);

        this.indices.clear();

        return buffer;
    }

    public BufferBuilder begin() {
        this.buffer.clear();
        this.indexed = false;

        return this;
    }

    public BufferBuilder beginQuads() {
        this.buffer.clear();

        if (this.indices == null) {
            this.indices = MemoryUtil.memAllocShort(512);
        }

        this.indices.clear();
        this.vertexCount = 0;
        this.indexCount = 0;
        this.indexed = true;

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

    public BufferBuilder endVertex() {
        if (this.indexed) {
            this.vertexCount++;

            if (this.vertexCount % 4 == 0) {
                int base = this.vertexCount - 4;

                this.indices.put((short) base);
                this.indices.put((short) (base + 1));
                this.indices.put((short) (base + 2));

                this.indices.put((short) (base + 2));
                this.indices.put((short) (base + 1));
                this.indices.put((short) (base + 3));

                this.indexCount += 6;
            }
        }

        return this;
    }

    public int getIndexCount() {
        return this.indexCount;
    }

    public void free() {
        if (this.indices != null) {
            MemoryUtil.memFree(this.indices);
            this.indices = null;
        }

        this.buffer = null;
    }
}