package com.theuran.pokoyo.vulkan.command;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

import java.nio.LongBuffer;

public class CommandPool {
    public final long pool;

    public CommandPool(Device device, int familyIndex, boolean supportReset) {
        IO.println("Creating Vulkan command pool");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo info = VkCommandPoolCreateInfo.calloc(stack)
                .sType$Default()
                .queueFamilyIndex(familyIndex);

            if (supportReset) {
                info.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            }

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(VK10.vkCreateCommandPool(device.device, info, null, buffer), "Failed to create command pool");

            this.pool = buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroyCommandPool(device.device, this.pool, null);
    }

    public void reset(Device device) {
        VK10.vkResetCommandPool(device.device, this.pool, 0);
    }
}