package com.theuran.pokoyo.vulkan.command;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.synchronization.Fence;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class CommandBuffer {
    private final boolean oneTimeSubmit;
    private final boolean primary;
    public final VkCommandBuffer buffer;

    public CommandBuffer(VulkanContext context, CommandPool pool, boolean primary, boolean oneTimeSubmit) {
        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo info = VkCommandBufferAllocateInfo.calloc(stack)
                .sType$Default()
                .commandPool(pool.pool)
                .level(primary ? VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                .commandBufferCount(1);

            this.buffer = new VkCommandBuffer(context.allocateCommandBuffer(info), context.device.device);
        }
    }

    public void submitAndWait(VulkanContext context, Queue queue) {
        Fence fence = new Fence(context, true);

        fence.reset(context);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferSubmitInfo.Buffer info = VkCommandBufferSubmitInfo.calloc(1, stack)
                .sType$Default()
                .commandBuffer(this.buffer);

            queue.submit(info, null, null, fence);
        }

        fence.await(context);
        fence.delete(context);
    }

    public void delete(VulkanContext context, CommandPool pool) {
        VK10.vkFreeCommandBuffers(context.device.device, pool.pool, this.buffer);
    }

    public void reset() {
        VK10.vkResetCommandBuffer(this.buffer, VK10.VK_COMMAND_BUFFER_RESET_RELEASE_RESOURCES_BIT);
    }

    public void beginWriting() {
        this.beginWriting(null);
    }

    public void beginWriting(InheritanceInfo inheritance) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo info = VkCommandBufferBeginInfo.calloc(stack).sType$Default();

            if (this.oneTimeSubmit) {
                info.flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            }

            if (!this.primary) {
                if (inheritance == null) {
                    throw new RuntimeException("Secondary buffers must declare inheritance info");
                }

                IntBuffer formats = stack.callocInt(inheritance.colorFormats.length);

                for (int format : inheritance.colorFormats) {
                    formats.put(0, format);
                }

                VkCommandBufferInheritanceRenderingInfo renderingInfo = VkCommandBufferInheritanceRenderingInfo.calloc(stack)
                    .sType$Default()
                    .depthAttachmentFormat(inheritance.depthFormat)
                    .pColorAttachmentFormats(formats)
                    .rasterizationSamples(inheritance.rasterizationSamples);

                VkCommandBufferInheritanceInfo inheritanceInfo = VkCommandBufferInheritanceInfo.calloc(stack)
                    .sType$Default()
                    .pNext(renderingInfo);

                info.pInheritanceInfo(inheritanceInfo);
            }

            int error = VK10.vkBeginCommandBuffer(this.buffer, info);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to begin write into command buffer", error);
            }
        }
    }

    public void endWriting() {
        int error = VK10.vkEndCommandBuffer(this.buffer);

        if (error != VK10.VK_SUCCESS) {
            throw new VulkanException("Failed to end write into command buffer", error);
        }
    }

    public static class InheritanceInfo {
        public int depthFormat;
        public int[] colorFormats;
        public int rasterizationSamples;

        public InheritanceInfo(int depthFormat, int[] colorFormats, int rasterizationSamples) {
            this.depthFormat = depthFormat;
            this.colorFormats = colorFormats;
            this.rasterizationSamples = rasterizationSamples;
        }
    }
}