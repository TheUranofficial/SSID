package com.theuran.pokoyo.vulkan.synchronization;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

public class Fence {
    public final long fence;

    public Fence(Device device, boolean signaled) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo info = VkFenceCreateInfo.calloc(stack)
                .sType$Default()
                .flags(signaled ? VK10.VK_FENCE_CREATE_SIGNALED_BIT : 0);

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(VK10.vkCreateFence(device.device, info, null, buffer), "Failed to create fence");

            this.fence = buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroyFence(device.device, this.fence, null);
    }

    public void fenceWait(Device device) {
        VK10.vkWaitForFences(device.device, this.fence, true, Long.MAX_VALUE);
    }

    public void reset(Device device) {
        VK10.vkResetFences(device.device, this.fence);
    }
}