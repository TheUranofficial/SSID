package com.theuran.pokoyo.vulkan;

import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.device.PhysicalDevice;
import com.theuran.pokoyo.vulkan.imageBuffer.Surface;
import com.theuran.pokoyo.vulkan.imageBuffer.SwapChain;
import com.theuran.pokoyo.vulkan.pipeline.PipelineCache;

public class VulkanContext {
    public final Device device;
    public final VulkanInstance instance;
    public final PhysicalDevice physicalDevice;
    public Surface surface;
    public SwapChain swapChain;
    public final Allocator allocator;
    public final PipelineCache pipelineCache;

    public VulkanContext() {
        this.instance = new VulkanInstance(true);
        this.physicalDevice = PhysicalDevice.createPhysicalDevice(this.instance, "Intel(R) UHD Graphics");
        this.device = new Device(this.physicalDevice);
        this.surface = new Surface(this.instance, this.physicalDevice);
        this.swapChain = new SwapChain(this.device, this.surface, 3, false);
        this.allocator = new Allocator(this.instance, this.physicalDevice, this.device);
        this.pipelineCache = new PipelineCache(this.device);
    }

    public void resize() {
        this.swapChain.delete(this.device);
        this.surface.delete(this.instance);
        this.surface = new Surface(this.instance, this.physicalDevice);
        this.swapChain = new SwapChain(this.device, this.surface, 3, false);
    }

    public void delete() {
        this.pipelineCache.delete(this.device);
        this.allocator.delete();
        this.swapChain.delete(this.device);
        this.surface.delete(this.instance);
        this.device.delete();
        this.physicalDevice.delete();
        this.instance.delete();
    }
}