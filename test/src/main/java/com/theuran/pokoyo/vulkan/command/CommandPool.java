package com.theuran.pokoyo.vulkan.command;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;

public class CommandPool implements IVulkanDisposable {
    public final long pool;

    public CommandPool(VulkanContext context, int familyIndex, boolean supportReset) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo info = VkCommandPoolCreateInfo.calloc(stack)
                .sType$Default()
                .queueFamilyIndex(familyIndex);

            if (supportReset) {
                info.flags(VK10.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            }

            this.pool = context.createCommandPool(info);
        }
    }

    public void reset(VulkanContext context) {
        VK10.vkResetCommandPool(context.device.device, this.pool, 0);
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroyCommandPool(context.device.device, this.pool, null);
    }
}