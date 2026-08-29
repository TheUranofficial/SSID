package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkBufferCreateInfo;

import java.nio.LongBuffer;

public class Buffer {
    private final long allocation;
    public final long buffer;
    private final PointerBuffer pointer;
    public final long requestedSize;
    private long mappedMemory;

    public Buffer(Allocator allocator, long size, int usage, int vmaUsage, int vmaFlags, int requestedFlags) {
        this.requestedSize = size;
        this.mappedMemory = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo info = VkBufferCreateInfo.calloc(stack)
                .sType$Default()
                .size(this.requestedSize)
                .usage(usage)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack)
                .usage(vmaUsage)
                .flags(vmaFlags)
                .requiredFlags(requestedFlags);

            PointerBuffer allocation = stack.callocPointer(1);
            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(Vma.vmaCreateBuffer(allocator.allocator, info, allocationInfo, buffer, allocation, null), "Failed to create buffer");

            this.buffer = buffer.get(0);
            this.allocation = allocation.get(0);
            this.pointer = MemoryUtil.memAllocPointer(1);
        }
    }

    public void delete(Allocator allocator) {
        this.unMap(allocator);
        MemoryUtil.memFree(this.pointer);
        Vma.vmaDestroyBuffer(allocator.allocator, this.buffer, this.allocation);
    }

    public void flush(Allocator allocator) {
        Vma.vmaFlushAllocation(allocator.allocator, this.allocation, 0, VK10.VK_WHOLE_SIZE);
    }

    public long map(Allocator allocator) {
        if (this.mappedMemory == 0) {
            VulkanUtils.checkError(Vma.vmaMapMemory(allocator.allocator, this.allocation, this.pointer), "Failed to map buffer");
            this.mappedMemory = this.pointer.get(0);
        }

        return this.mappedMemory;
    }

    public void unMap(Allocator allocator) {
        if (this.mappedMemory != 0) {
            Vma.vmaUnmapMemory(allocator.allocator, this.allocation);
            this.mappedMemory = 0;
        }
    }
}