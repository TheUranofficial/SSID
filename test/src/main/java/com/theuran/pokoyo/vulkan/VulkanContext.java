package com.theuran.pokoyo.vulkan;

import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.device.PhysicalDevice;
import com.theuran.pokoyo.vulkan.imageBuffer.Surface;
import com.theuran.pokoyo.vulkan.imageBuffer.SwapChain;
import com.theuran.pokoyo.vulkan.pipeline.PipelineCache;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import mchorse.bbs.core.IDisposable;
import mchorse.bbs.graphics.window.Window;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanContext implements IDisposable {
    public final Device device;
    public final VulkanInstance instance;
    public final PhysicalDevice physicalDevice;
    public Surface surface;
    public SwapChain swapChain;
    public final Allocator allocator;
    public final PipelineCache pipelineCache;

    public VulkanContext() {
        this.instance = new VulkanInstance(true);
        this.physicalDevice = PhysicalDevice.createPhysicalDevice(this, "Intel(R) UHD Graphics");
        this.device = new Device(this);
        this.surface = new Surface(this);
        this.swapChain = new SwapChain(this, 3, false);
        this.allocator = new Allocator(this);
        this.pipelineCache = new PipelineCache(this);
    }

    public void resize() {
        this.swapChain.delete(this);
        this.surface.delete(this);
        this.surface = new Surface(this);
        this.swapChain = new SwapChain(this, 3, false);
    }

    @Override
    public void delete() {
        this.pipelineCache.delete(this);
        this.allocator.delete();
        this.swapChain.delete(this);
        this.surface.delete(this);
        this.device.delete();
        this.physicalDevice.delete();
        this.instance.delete();
    }

    /* Raw helpers for create handles */

    public long createFence(VkFenceCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreateFence(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create fence", error);
            }

            return buffer.get(0);
        }
    }

    public long createSemaphore(VkSemaphoreCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreateSemaphore(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create semaphore", error);
            }

            return buffer.get(0);
        }
    }

    public long allocateCommandBuffer(VkCommandBufferAllocateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer buffer = stack.mallocPointer(1);
            int error = VK10.vkAllocateCommandBuffers(this.device.device, info, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to allocate command buffer", error);
            }

            return buffer.get(0);
        }
    }

    public long createCommandPool(VkCommandPoolCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreateCommandPool(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create command pool", error);
            }

            return buffer.get(0);
        }
    }

    public long createImageView(VkImageViewCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreateImageView(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create image view", error);
            }

            return buffer.get(0);
        }
    }

    public long createSwapChain(VkSwapchainCreateInfoKHR info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = KHRSwapchain.vkCreateSwapchainKHR(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create image view", error);
            }

            return buffer.get(0);
        }
    }

    public void createBuffer(VkBufferCreateInfo info, VmaAllocationCreateInfo allocationInfo, LongBuffer buffer, PointerBuffer allocation) {
        int error = Vma.vmaCreateBuffer(this.allocator.allocator, info, allocationInfo, buffer, allocation, null);

        if (error != VK10.VK_SUCCESS) {
            throw new VulkanException("Failed to create buffer", error);
        }
    }

    public long createPipelineLayout(VkPipelineLayoutCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreatePipelineLayout(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create pipeline layout", error);
            }

            return buffer.get(0);
        }
    }

    public long createPipeline(VkGraphicsPipelineCreateInfo.Buffer info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.callocLong(1);
            int error = VK10.vkCreateGraphicsPipelines(this.device.device, this.pipelineCache.cache, info, null, buffer);
            long handle = buffer.get(0);

            IO.println("vkCreateGraphicsPipelines: result=" + error + ", handle=0x" + Long.toHexString(handle));

            if (error != VK10.VK_SUCCESS || handle == VK10.VK_NULL_HANDLE) {
                throw new VulkanException("Failed to create pipeline", error);
            }

            return handle;
        }
    }

    public long createShader(VkShaderModuleCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreateShaderModule(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create shader", error);
            }

            return buffer.get(0);
        }
    }

    public void createImage(VkImageCreateInfo info, VmaAllocationCreateInfo allocationInfo, LongBuffer buffer, PointerBuffer allocation) {
        int error = Vma.vmaCreateImage(this.allocator.allocator, info, allocationInfo, buffer, allocation, null);

        if (error != VK10.VK_SUCCESS) {
            throw new VulkanException("Failed to create image", error);
        }
    }

    public long createPipelineCache(VkPipelineCacheCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            int error = VK10.vkCreatePipelineCache(this.device.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Error creating pipeline cache", error);
            }

            return buffer.get(0);
        }
    }

    public long createWindowSurface() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer surfaceBuffer = stack.mallocLong(1);

            GLFWVulkan.glfwCreateWindowSurface(this.instance.instance, Window.getWindow(), null, surfaceBuffer);

            return surfaceBuffer.get(0);
        }
    }

    public VkSurfaceCapabilitiesKHR getDeviceSurface(long surface) {
        VkSurfaceCapabilitiesKHR capabilities = VkSurfaceCapabilitiesKHR.calloc();
        int error = KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(this.physicalDevice.device, surface, capabilities);

        if (error != VK10.VK_SUCCESS) {
            throw new VulkanException("Failed to get surface capabilities", error);
        }

        return capabilities;
    }

    public VkDevice createDevice(VkDeviceCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer buffer = stack.mallocPointer(1);
            int error = VK10.vkCreateDevice(this.physicalDevice.device, info, null, buffer);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create logical device", error);
            }

            return new VkDevice(buffer.get(0), this.physicalDevice.device, info);
        }
    }

    public long createAllocator(VmaAllocatorCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer allocator = stack.mallocPointer(1);
            int error = Vma.vmaCreateAllocator(info, allocator);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to create Vulkan Memory Allocator", error);
            }

            return allocator.get(0);
        }
    }
}