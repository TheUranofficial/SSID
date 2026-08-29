package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UploadContext {
    private final List<Buffer> pendingStaging = new ArrayList<>();

    public Buffer upload(ByteBuffer data, Allocator allocator, CommandBuffer commandBuffer, int usage) {
        long size = data.remaining();

        if (size == 0) {
            throw new IllegalArgumentException("Empty ByteBuffer for UploadContext.upload");
        }

        Buffer staging = new Buffer(allocator, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, Vma.VMA_MEMORY_USAGE_AUTO, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT, 0);

        long mapped = staging.map(allocator);

        MemoryUtil.memByteBuffer(mapped, (int) size).put(data.duplicate());
        staging.flush(allocator);
        staging.unMap(allocator);

        Buffer gpuBuffer = new Buffer(allocator, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage, Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE, 0, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy.Buffer copy = VkBufferCopy.calloc(1, stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);

            VK10.vkCmdCopyBuffer(commandBuffer.buffer, staging.buffer, gpuBuffer.buffer, copy);
        }

        this.pendingStaging.add(staging);

        return gpuBuffer;
    }

    public Buffer uploadVertexBuffer(ByteBuffer data, Allocator allocator, CommandBuffer commandBuffer) {
        return this.upload(data, allocator, commandBuffer, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
    }

    public void delete(Allocator allocator) {
        for (Buffer buffer : this.pendingStaging) {
            buffer.delete(allocator);
        }

        this.pendingStaging.clear();
    }
}
