package com.theuran.pokoyo.vulkan.command;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.device.PhysicalDevice;
import com.theuran.pokoyo.vulkan.synchronization.Fence;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

public class Queue {
    public final int index;
    public final VkQueue queue;

    public Queue(Device device, int index, int queueIndex) {
        IO.println("Creating queue");

        this.index = index;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer queueBuffer = stack.mallocPointer(1);

            VK10.vkGetDeviceQueue(device.device, index, queueIndex, queueBuffer);

            long queue = queueBuffer.get(0);

            this.queue = new VkQueue(queue, device.device);
        }
    }

    public void waitIdle() {
        VK10.vkQueueWaitIdle(this.queue);
    }

    public void submit(VkCommandBufferSubmitInfo.Buffer buffers, VkSemaphoreSubmitInfo.Buffer waitSemaphores, VkSemaphoreSubmitInfo.Buffer signalSemaphores, Fence fence) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSubmitInfo2.Buffer info = VkSubmitInfo2.calloc(1, stack)
                .sType$Default()
                .pCommandBufferInfos(buffers)
                .pSignalSemaphoreInfos(signalSemaphores);

            if (waitSemaphores != null) {
                info.pWaitSemaphoreInfos(waitSemaphores);
            }

            VulkanUtils.checkError(VK13.vkQueueSubmit2(this.queue, info, fence != null ? fence.fence : 0), "Failed to submit command to queue");
        }
    }

    public static class GraphicsQueue extends Queue {
        public GraphicsQueue(VulkanContext context, int queueIndex) {
            super(context.device, getGraphicsFamilyIndex(context.physicalDevice), queueIndex);
        }

        public static int getGraphicsFamilyIndex(PhysicalDevice device) {
            VkQueueFamilyProperties.Buffer buffer = device.familyProperties;

            for (int i = 0; i < buffer.capacity(); i++) {
                if ((buffer.get(i).queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
                    return i;
                }
            }

            throw new RuntimeException("Failed to get graphics Queue family index");
        }
    }

    public static class PresentQueue extends Queue {
        public PresentQueue(VulkanContext context, int queueIndex) {
            super(context.device, getPresentFamilyIndex(context), queueIndex);
        }

        private static int getPresentFamilyIndex(VulkanContext context) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkQueueFamilyProperties.Buffer propertiesBuffer = context.physicalDevice.familyProperties;
                IntBuffer buffer = stack.mallocInt(1);

                for (int i = 0; i < buffer.capacity(); i++) {
                    KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(context.physicalDevice.device, i, context.surface.surface, buffer);

                    if (buffer.get(0) == VK10.VK_TRUE) {
                        return i;
                    }
                }
            }

            throw new RuntimeException("Failed to get Presentation Queue family index");
        }
    }
}