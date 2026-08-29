package com.theuran.pokoyo.vulkan.command;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.synchronization.Fence;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class CommandBuffer {
    private final boolean oneTimeSubmit;
    private final boolean primary;
    public final VkCommandBuffer buffer;

    public CommandBuffer(Device device, CommandPool pool, boolean primary, boolean oneTimeSubmit) {
        IO.println("Creating command buffer");

        this.primary = primary;
        this.oneTimeSubmit = oneTimeSubmit;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo info = VkCommandBufferAllocateInfo.calloc(stack)
                .sType$Default()
                .commandPool(pool.pool)
                .level(primary ? VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VK10.VK_COMMAND_BUFFER_LEVEL_SECONDARY)
                .commandBufferCount(1);

            PointerBuffer buffer = stack.mallocPointer(1);

            VulkanUtils.checkError(VK10.vkAllocateCommandBuffers(device.device, info, buffer), "Failed to allocate render command buffer");

            this.buffer = new VkCommandBuffer(buffer.get(0), device.device);
        }
    }

    public void submitAndWait(Device device, Queue queue) {
        Fence fence = new Fence(device, true);

        fence.reset(device);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferSubmitInfo.Buffer info = VkCommandBufferSubmitInfo.calloc(1, stack)
                .sType$Default()
                .commandBuffer(this.buffer);

            queue.submit(info, null, null, fence);
        }

        fence.fenceWait(device);
        fence.delete(device);
    }

    public void delete(Device device, CommandPool pool) {
        VK10.vkFreeCommandBuffers(device.device, pool.pool, this.buffer);
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

            VulkanUtils.checkError(VK10.vkBeginCommandBuffer(this.buffer, info), "Failed to begin command buffer");
        }
    }

    public void endWriting() {
        VulkanUtils.checkError(VK10.vkEndCommandBuffer(this.buffer), "Failed to end command buffer");
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