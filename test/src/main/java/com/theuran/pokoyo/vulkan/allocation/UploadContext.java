package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import com.theuran.pokoyo.vulkan.command.CommandPool;
import com.theuran.pokoyo.vulkan.command.Queue;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCopy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UploadContext implements IVulkanDisposable {
    private CommandPool pool;
    private CommandBuffer buffer;
    private final List<Buffer> pendingStaging = new ArrayList<>();

    public UploadContext(VulkanContext context, int queueIndex) {
        this.pool = new CommandPool(context, queueIndex, false);
        this.buffer = new CommandBuffer(context, this.pool, true, true);
    }

    public void begin(VulkanContext context) {
        this.pool.reset(context);
        this.buffer.beginWriting();
    }

    public Buffer upload(VulkanContext context, ByteBuffer data, int usage) {
        long size = data.remaining();

        if (size == 0) {
            throw new IllegalArgumentException("Empty ByteBuffer for UploadContext.upload");
        }

        Buffer staging = new Buffer(context, size, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, Vma.VMA_MEMORY_USAGE_AUTO, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT, 0);
        long mapped = staging.map(context);

        MemoryUtil.memByteBuffer(mapped, (int) size).put(data.duplicate());

        staging.flush(context);
        staging.unMap(context);

        Buffer gpuBuffer = new Buffer(context, size, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | usage, Vma.VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE, 0, 0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy.Buffer copy = VkBufferCopy.calloc(1, stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);

            VK10.vkCmdCopyBuffer(this.buffer.buffer, staging.buffer, gpuBuffer.buffer, copy);
        }

        this.pendingStaging.add(staging);

        return gpuBuffer;
    }

    public Buffer uploadVertexBuffer(VulkanContext context, ByteBuffer data) {
        return this.upload(context, data, VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
    }

    public Buffer uploadIndexBuffer(VulkanContext context, ByteBuffer data) {
        return this.upload(context, data, VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
    }

    public void end(VulkanContext context, Queue.GraphicsQueue queue) {
        this.buffer.endWriting();
        this.buffer.submitAndWait(context, queue);

        for (Buffer buffer : this.pendingStaging) {
            buffer.delete(context);
        }

        this.pendingStaging.clear();
    }

    @Override
    public void delete(VulkanContext context) {
        for (Buffer buffer : this.pendingStaging) {
            buffer.delete(context);
        }

        this.pendingStaging.clear();

        this.buffer.delete(context, this.pool);
        this.pool.delete(context);
    }
}