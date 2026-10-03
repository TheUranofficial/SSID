package com.theuran.pokoyo.vulkan.synchronization;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFenceCreateInfo;

public class Fence implements IVulkanDisposable {
    public final long fence;

    public Fence(VulkanContext context, boolean signaled) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo info = VkFenceCreateInfo.calloc(stack)
                .sType$Default()
                .flags(signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0);

            this.fence = context.createFence(info);
        }
    }

    public void await(VulkanContext context) {
        VK10.vkWaitForFences(context.device.device, this.fence, true, Long.MAX_VALUE);
    }

    public void reset(VulkanContext context) {
        VK10.vkResetFences(context.device.device, this.fence);
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroyFence(context.device.device, this.fence, null);
    }
}