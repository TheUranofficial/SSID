package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;

import java.nio.LongBuffer;

public class PipelineCache {
    public final long cache;

    public PipelineCache(Device device) {
        IO.println("Creating cache for pipelines for device");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineCacheCreateInfo info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default();

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(VK10.vkCreatePipelineCache(device.device, info, null, buffer), "Error creating pipeline cache");

            this.cache = buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroyPipelineCache(device.device, this.cache, null);
    }
}
