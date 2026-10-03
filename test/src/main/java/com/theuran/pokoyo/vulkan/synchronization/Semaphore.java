package com.theuran.pokoyo.vulkan.synchronization;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

public class Semaphore implements IVulkanDisposable {
    public final long semaphore;

    public Semaphore(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo info = VkSemaphoreCreateInfo.calloc(stack).sType$Default();

            this.semaphore = context.createSemaphore(info);
        }
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroySemaphore(context.device.device, this.semaphore, null);
    }
}