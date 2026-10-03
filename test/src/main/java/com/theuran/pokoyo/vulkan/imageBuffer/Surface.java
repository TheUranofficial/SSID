package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;

public class Surface implements IVulkanDisposable {
    public final long surface;
    public final VkSurfaceCapabilitiesKHR capabilities;
    public final Format format;

    public Surface(VulkanContext context) {
        this.surface = context.createWindowSurface();
        this.capabilities = context.getDeviceSurface(this.surface);
        this.format = this.calculateSurfaceFormat(context);
    }

    private Format calculateSurfaceFormat(VulkanContext context) {
        int imageFormat;
        int colorSpace;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer formatBuffer = stack.callocInt(1);
            int error = KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(context.physicalDevice.device, this.surface, formatBuffer, null);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get the number surface formats", error);
            }

            int count = formatBuffer.get(0);

            if (count <= 0) {
                throw new RuntimeException("No surface formats retrieved");
            }

            VkSurfaceFormatKHR.Buffer formats = VkSurfaceFormatKHR.calloc(count, stack);

            error = KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(context.physicalDevice.device, this.surface, formatBuffer, formats);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get the number surface formats", error);
            }

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

    @Override
    public void delete(VulkanContext context) {
        this.capabilities.free();
        KHRSurface.vkDestroySurfaceKHR(context.instance.instance, this.surface, null);
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