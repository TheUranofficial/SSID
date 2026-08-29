package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanInstance;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.PhysicalDevice;
import mchorse.bbs.graphics.window.Window;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class Surface {
    public final long surface;
    public final VkSurfaceCapabilitiesKHR capabilities;
    public final Format format;

    public Surface(VulkanInstance instance, PhysicalDevice device) {
        IO.println("Creating Vulkan surface");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer surfaceBuffer = stack.mallocLong(1);

            GLFWVulkan.glfwCreateWindowSurface(instance.instance, Window.getWindow(), null, surfaceBuffer);

            this.surface = surfaceBuffer.get(0);
            this.capabilities = VkSurfaceCapabilitiesKHR.calloc();

            VulkanUtils.checkError(KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.device, this.surface, this.capabilities), "Failed to get surface capabilities");

            this.format = this.calculateSurfaceFormat(device, this.surface);
        }
    }

    private Format calculateSurfaceFormat(PhysicalDevice device, long surface) {
        int imageFormat;
        int colorSpace;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer formatBuffer = stack.callocInt(1);

            VulkanUtils.checkError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device.device, this.surface, formatBuffer, null), "Failed to get the number surface formats");

            int count = formatBuffer.get(0);

            if (count <= 0) {
                throw new RuntimeException("No surface formats retrieved");
            }

            VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.calloc(count, stack);

            VulkanUtils.checkError(KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device.device, this.surface, formatBuffer, formats), "Failed to get surface formats");

            imageFormat = VK10.VK_FORMAT_B8G8R8A8_SRGB;
            colorSpace = formats.get(0).colorSpace();

            for (int i = 0; i < count; i++) {
                VkSurfaceFormatKHR format = formats.get(i);

                if (format.format() == VK10.VK_FORMAT_B8G8R8A8_SRGB && format.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    colorSpace = format.colorSpace();

                    break;
                }
            }
        }

        return new Format(imageFormat, colorSpace);
    }

    public void delete(VulkanInstance instance) {
        this.capabilities.free();
        KHRSurface.vkDestroySurfaceKHR(instance.instance, this.surface, null);
    }

    public static class Format {
        public int imageFormat;
        public int colorSpace;

        public Format(int imageFormat, int colorSpace) {
            this.imageFormat = imageFormat;
            this.colorSpace = colorSpace;
        }
    }
}