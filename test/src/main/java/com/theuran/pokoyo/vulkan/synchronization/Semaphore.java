package com.theuran.pokoyo.vulkan.synchronization;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

public class Semaphore {
    public final long semaphore;

    public Semaphore(Device device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo info = VkSemaphoreCreateInfo.calloc(stack).sType$Default();

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(VK10.vkCreateSemaphore(device.device, info, null, buffer), "Failed to create semaphore");

            this.semaphore = buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroySemaphore(device.device, this.semaphore, null);
    }
}