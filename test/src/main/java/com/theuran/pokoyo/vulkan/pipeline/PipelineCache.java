package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;

public class PipelineCache implements IVulkanDisposable {
    public final long cache;

    public PipelineCache(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineCacheCreateInfo info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default();

            this.cache = context.createPipelineCache(info);
        }
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroyPipelineCache(context.device.device, this.cache, null);
    }
}
