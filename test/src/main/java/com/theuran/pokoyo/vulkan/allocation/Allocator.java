package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanContext;
import mchorse.bbs.core.IDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK13;

public class Allocator implements IDisposable {
    public long allocator;

    public Allocator(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions functions = VmaVulkanFunctions.calloc(stack)
                .set(context.instance.instance, context.device.device);

            VmaAllocatorCreateInfo info = VmaAllocatorCreateInfo.calloc(stack)
                .vulkanApiVersion(VK13.VK_API_VERSION_1_3)
                .physicalDevice(context.physicalDevice.device)
                .device(context.device.device)
                .instance(context.instance.instance)
                .pVulkanFunctions(functions);

            this.allocator = context.createAllocator(info);
        }
    }

    @Override
    public void delete() {
        Vma.vmaDestroyAllocator(this.allocator);
    }
}
