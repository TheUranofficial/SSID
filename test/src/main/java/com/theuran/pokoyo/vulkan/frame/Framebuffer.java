package com.theuran.pokoyo.vulkan.frame;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import com.theuran.pokoyo.vulkan.command.CommandPool;
import com.theuran.pokoyo.vulkan.synchronization.Fence;
import com.theuran.pokoyo.vulkan.synchronization.Semaphore;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;

public class Framebuffer implements IVulkanDisposable {
    public final CommandPool commandPool;
    public final CommandBuffer commandBuffer;
    public final Fence fence;
    public Semaphore presentSemaphore;

    public Framebuffer(VulkanContext context, int queueIndex) {
        this.commandPool = new CommandPool(context, queueIndex, false);
        this.commandBuffer = new CommandBuffer(context, this.commandPool, true, true);
        this.fence = new Fence(context, true);
        this.presentSemaphore = new Semaphore(context);
    }

    public void startWriting(VulkanContext context) {
        this.commandPool.reset(context);
        this.commandBuffer.beginWriting();
    }

    public void stopWriting() {
        this.commandBuffer.endWriting();
    }

    public void waitForFence(VulkanContext context) {
        this.fence.await(context);
    }

    public void resetFence(VulkanContext context) {
        this.fence.reset(context);
    }

    @Override
    public void delete(VulkanContext context) {
        this.presentSemaphore.delete(context);
        this.fence.delete(context);
        this.commandBuffer.delete(context, this.commandPool);
        this.commandPool.delete(context);
    }
}