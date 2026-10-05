package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;

import java.nio.ByteBuffer;

public class FrameArena implements IVulkanDisposable {
    public static final int MIN_ALIGNMENT = 16;

    private final Buffer buffer;
    private final long mapped;
    private final long baseAddress;
    private final long capacity;
    public long offset = 0;

    public FrameArena(VulkanContext context, long capacity) {
        this.capacity = capacity;
        this.buffer = new Buffer(context, capacity, VK12.VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT, Vma.VMA_MEMORY_USAGE_AUTO, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT | Vma.VMA_ALLOCATION_CREATE_MAPPED_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
        this.mapped = this.buffer.map(context);
        this.baseAddress = this.buffer.getDeviceAddress(context);
    }

    public long pushData(ByteBuffer data, int alignment) {
        long size = data.remaining();
        long align = Math.max(alignment, MIN_ALIGNMENT);
        long aligned = (this.offset + align - 1) & -align;

        if (aligned + size > this.capacity) {
            throw new IllegalStateException("FrameArena overflow: need " + (aligned + size) + " of " + this.capacity);
        }

        MemoryUtil.memCopy(MemoryUtil.memAddress(data), this.mapped + aligned, size);

        this.offset = aligned + size;

        return this.baseAddress + aligned;
    }

    @Override
    public void delete(VulkanContext context) {
        this.buffer.delete(context);
    }
}