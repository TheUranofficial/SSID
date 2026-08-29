package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.command.Queue;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.synchronization.Semaphore;
import mchorse.bbs.graphics.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class SwapChain {
    public final ImageView[] imageViews;
    public final VkExtent2D extent;
    private final long swapChain;

    public SwapChain(Device device, Surface surface, int requestedImages, boolean vsync) {
        IO.println("Creating Vulkan SwapChain");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR surfaceCapabilities = surface.capabilities;

            int images = this.calculateCountChain(surfaceCapabilities, requestedImages);

            this.extent = this.calculateSwapChainExtent(surfaceCapabilities);

            VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType$Default()
                .surface(surface.surface)
                .minImageCount(images)
                .imageFormat(surface.format.imageFormat)
                .imageColorSpace(surface.format.colorSpace)
                .imageExtent(this.extent)
                .imageArrayLayers(1)
                .imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                .preTransform(surfaceCapabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .clipped(true);

            if (vsync) {
                info.presentMode(KHRSurface.VK_PRESENT_MODE_FIFO_KHR);
            } else {
                info.presentMode(KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR);
            }

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(KHRSwapchain.vkCreateSwapchainKHR(device.device, info, null, buffer), "Failed to create swap chain");

            this.swapChain = buffer.get(0);
            this.imageViews = this.createImageViews(stack, device, surface.format.imageFormat);
        }
    }

    public void delete(Device device) {
        this.extent.free();

        for (ImageView view : this.imageViews) {
            view.delete(device);
        }

        KHRSwapchain.vkDestroySwapchainKHR(device.device, this.swapChain, null);
    }

    private ImageView[] createImageViews(MemoryStack stack, Device device, int imageFormat) {
        IntBuffer buffer = stack.mallocInt(1);

        VulkanUtils.checkError(KHRSwapchain.vkGetSwapchainImagesKHR(device.device, this.swapChain, buffer, null), "Failed to get number of surface images");

        int count = buffer.get(0);

        LongBuffer images = stack.mallocLong(count);

        VulkanUtils.checkError(KHRSwapchain.vkGetSwapchainImagesKHR(device.device, swapChain, buffer, images), "Failed to get surface images");

        ImageView[] views = new ImageView[count];
        ImageView.Data data = new ImageView.Data().format(imageFormat).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);

        for (int i = 0; i < count; i++) {
            views[i] = new ImageView(device, images.get(i), data);
        }

        return views;
    }

    private VkExtent2D calculateSwapChainExtent(VkSurfaceCapabilitiesKHR surfaceCapabilities) {
        VkExtent2D result = VkExtent2D.calloc();

        if (surfaceCapabilities.currentExtent().width() == 0xffffffff) {
            int width = Math.clamp(Window.getWidth(), surfaceCapabilities.minImageExtent().width(), surfaceCapabilities.maxImageExtent().width());
            int height = Math.clamp(Window.getHeight(), surfaceCapabilities.minImageExtent().height(), surfaceCapabilities.maxImageExtent().height());

            result.width(width);
            result.height(height);
        } else {
            result.set(surfaceCapabilities.currentExtent());
        }

        return result;
    }

    private int calculateCountChain(VkSurfaceCapabilitiesKHR surfaceCapabilities, int requestedImages) {
        int maxImages = surfaceCapabilities.maxImageCount();
        int minImages = surfaceCapabilities.minImageCount();
        int result = minImages;

        if (maxImages != 0) {
            result = Math.min(requestedImages, maxImages);
        }

        result = Math.max(result, minImages);

        IO.println("Requested " + requestedImages + " images, got " + result + " images. Surface capabilities, maxImages: " + maxImages + " minImages: " + minImages);

        return result;
    }

    public int acquireNextImage(Device device, Semaphore semaphore) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            int error = KHRSwapchain.vkAcquireNextImageKHR(device.device, this.swapChain, ~0L, semaphore.semaphore, 0, buffer);

            if (error == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                return -1;
            } else if (error == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                /* Not optimal but swap chain can still be used */
            } else if (error != VK10.VK_SUCCESS) {
                throw new RuntimeException("Failed to acquire image: " + error);
            }

            return buffer.get(0);
        }
    }

    public boolean presentImage(Queue.PresentQueue presentQueue, Semaphore renderSemaphores, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPresentInfoKHR info = VkPresentInfoKHR.calloc(stack)
                .sType$Default()
                .pWaitSemaphores(stack.longs(renderSemaphores.semaphore))
                .swapchainCount(1)
                .pSwapchains(stack.longs(this.swapChain))
                .pImageIndices(stack.ints(imageIndex));

            int error = KHRSwapchain.vkQueuePresentKHR(presentQueue.queue, info);

            if (error == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
                return true;
            } else if (error == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                /* Not optimal but swap chain can still be used */
            } else if (error != VK13.VK_SUCCESS) {
                throw new RuntimeException("Failed to present KHR: " + error);
            }
        }

        return false;
    }
}