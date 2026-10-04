package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class Buffer implements IVulkanDisposable {
    private final long allocation;
    public final long buffer;
    private final PointerBuffer pointer;
    public final long requestedSize;
    private long mappedMemory;

    public Buffer(VulkanContext context, long size, int usage, int vmaUsage, int vmaFlags, int requestedFlags) {
        this.requestedSize = size;
        this.mappedMemory = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo info = VkBufferCreateInfo.calloc(stack)
                .sType$Default()
                .size(this.requestedSize)
                .usage(usage)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            int flags = vmaFlags;

            if ((usage & VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT) != 0) {
                flags |= Vma.VMA_ALLOCATION_CREATE_DONT_BIND_BIT;
            }

            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(stack)
                .usage(vmaUsage)
                .flags(flags)
                .requiredFlags(requestedFlags);

            PointerBuffer allocation = stack.callocPointer(1);
            LongBuffer buffer = stack.mallocLong(1);

            context.createBuffer(info, allocationInfo, buffer, allocation);

            this.buffer = buffer.get(0);
            this.allocation = allocation.get(0);
            this.pointer = MemoryUtil.memAllocPointer(1);
        }
    }

    public long getDeviceAddress(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferDeviceAddressInfo buffer = VkBufferDeviceAddressInfo.calloc(stack)
                .sType$Default()
                .buffer(this.buffer);

            return VK12.vkGetBufferDeviceAddress(context.device.device, buffer);
        }
    }

    public void flush(VulkanContext context) {
        Vma.vmaFlushAllocation(context.allocator.allocator, this.allocation, 0, VK10.VK_WHOLE_SIZE);
    }

    public long map(VulkanContext context) {
        if (this.mappedMemory == 0) {
            int error = Vma.vmaMapMemory(context.allocator.allocator, this.allocation, this.pointer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to map buffer", error);
            }

            this.mappedMemory = this.pointer.get(0);
        }

        return this.mappedMemory;
    }

    public void unMap(VulkanContext context) {
        if (this.mappedMemory != 0) {
            Vma.vmaUnmapMemory(context.allocator.allocator, this.allocation);
            this.mappedMemory = 0;
        }
    }

    @Override
    public void delete(VulkanContext context) {
        this.unMap(context);
        MemoryUtil.memFree(this.pointer);
        Vma.vmaDestroyBuffer(context.allocator.allocator, this.buffer, this.allocation);
    }
}