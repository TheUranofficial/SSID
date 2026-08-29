package com.theuran.pokoyo.vulkan.allocation;

import com.theuran.pokoyo.vulkan.VulkanInstance;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.device.PhysicalDevice;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK13;

public class Allocator {
    public long allocator;

    public Allocator(VulkanInstance instance, PhysicalDevice physicalDevice, Device device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VmaVulkanFunctions functions = VmaVulkanFunctions.calloc(stack);

            VmaAllocatorCreateInfo info = VmaAllocatorCreateInfo.calloc(stack)
                .vulkanApiVersion(VK13.VK_API_VERSION_1_3)
                .physicalDevice(physicalDevice.device)
                .device(device.device)
                .instance(instance.instance)
                .pVulkanFunctions(functions);

            PointerBuffer allocator = stack.mallocPointer(1);

            VulkanUtils.checkError(Vma.vmaCreateAllocator(info, allocator), "Failed to create Vulkan Memory Allocator");

            this.allocator = allocator.get(0);
        }
    }

    public void delete() {
        Vma.vmaDestroyAllocator(this.allocator);
    }
}
