package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.command.Queue;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.synchronization.Semaphore;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import mchorse.bbs.graphics.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class SwapChain implements IVulkanDisposable {
    public final ImageView[] imageViews;
    public final VkExtent2D extent;
    private final long swapChain;
    public Semaphore[] renderSemaphores;

    public SwapChain(VulkanContext context, int requestedImages, boolean vsync) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSurfaceCapabilitiesKHR surfaceCapabilities = context.surface.capabilities;
            int images = this.calculateCountChain(surfaceCapabilities, requestedImages);

            this.extent = this.calculateSwapChainExtent(surfaceCapabilities);

            VkSwapchainCreateInfoKHR info = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType$Default()
                .surface(context.surface.surface)
                .minImageCount(images)
                .imageFormat(context.surface.format.imageFormat)
                .imageColorSpace(context.surface.format.colorSpace)
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

            this.swapChain = context.createSwapChain(info);
            this.imageViews = this.createImageViews(context);
            this.renderSemaphores = new Semaphore[this.imageViews.length];

            for (int i = 0; i < this.imageViews.length; i++) {
                this.renderSemaphores[i] = new Semaphore(context);
            }
        }
    }

    @Override
    public void delete(VulkanContext context) {
        for (Semaphore semaphore : this.renderSemaphores) {
            semaphore.delete(context);
        }

        this.extent.free();

        for (ImageView view : this.imageViews) {
            view.delete(context);
        }

        KHRSwapchain.vkDestroySwapchainKHR(context.device.device, this.swapChain, null);
    }

    private ImageView[] createImageViews(VulkanContext context) {
        long[] images = this.getImages(context);
        ImageView[] views = new ImageView[images.length];
        ImageView.Data data = new ImageView.Data().format(context.surface.format.imageFormat).aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);

        for (int i = 0; i < images.length; i++) {
            views[i] = new ImageView(context, images[i], data);
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

    private long[] getImages(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer buffer = stack.mallocInt(1);
            int error = KHRSwapchain.vkGetSwapchainImagesKHR(context.device.device, this.swapChain, buffer, null);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get number of surface images", error);
            }

            LongBuffer images = stack.mallocLong(buffer.get(0));

            buffer.rewind();

            error = KHRSwapchain.vkGetSwapchainImagesKHR(context.device.device, this.swapChain, buffer, images);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get surface images", error);
            }

            long[] result = new long[buffer.get(0)];

            images.get(result);

            return result;
        }
    }
}