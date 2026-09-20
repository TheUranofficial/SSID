package com.theuran.pokoyo;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.allocation.UploadContext;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import com.theuran.pokoyo.vulkan.command.CommandPool;
import com.theuran.pokoyo.vulkan.command.Queue;
import com.theuran.pokoyo.vulkan.synchronization.Fence;
import com.theuran.pokoyo.vulkan.synchronization.Semaphore;
import mchorse.bbs.core.IDisposable;
import mchorse.bbs.graphics.window.Window;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class VulkanRenderer implements IDisposable {
    private final CommandBuffer[] commandBuffers;
    public final CommandPool[] commandPools;
    private final Fence[] fences;
    public final Queue.GraphicsQueue graphicsQueue;
    private final Semaphore[] presentSemaphores;
    private final Queue.PresentQueue presentQueue;
    private final Semaphore[] renderSemaphores;
    private final Render render;
    private final VulkanContext context = new VulkanContext();
    private int currentFrame = 0;
    private boolean resize = false;

    public UploadContext upload = new UploadContext();
    public Projection projection = new Projection(90, 1, 100, Window.getWidth(), Window.getHeight());

    public VulkanRenderer() {
        this.graphicsQueue = new Queue.GraphicsQueue(this.context, 0);
        this.presentQueue = new Queue.PresentQueue(this.context, 0);

        this.commandPools = new CommandPool[VulkanUtils.MAX_IN_FLIGHT];
        this.commandBuffers = new CommandBuffer[VulkanUtils.MAX_IN_FLIGHT];

        this.fences = new Fence[VulkanUtils.MAX_IN_FLIGHT];
        this.presentSemaphores = new Semaphore[VulkanUtils.MAX_IN_FLIGHT];
        this.renderSemaphores = new Semaphore[this.context.swapChain.imageViews.length];

        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            this.commandPools[i] = new CommandPool(this.context.device, this.graphicsQueue.index, false);
            this.commandBuffers[i] = new CommandBuffer(this.context.device, this.commandPools[i], true, true);

            this.presentSemaphores[i] = new Semaphore(this.context.device);
            this.fences[i] = new Fence(this.context.device, true);
        }

        for (int i = 0; i < this.context.swapChain.imageViews.length; i++) {
            this.renderSemaphores[i] = new Semaphore(this.context.device);
        }

        this.render = new Render(this.context, this);
    }

    @Override
    public void delete() {
        this.context.device.waitIdle();

        this.upload.delete(this.context.allocator);
        this.render.delete();

        for (Semaphore semaphore : this.renderSemaphores) {
            semaphore.delete(this.context.device);
        }

        for (Semaphore semaphore : this.presentSemaphores) {
            semaphore.delete(this.context.device);
        }

        for (Fence fence : this.fences) {
            fence.delete(this.context.device);
        }

        for (int i = 0; i < this.commandPools.length; i++) {
            this.commandBuffers[i].delete(this.context.device, this.commandPools[i]);
            this.commandPools[i].delete(this.context.device);
        }

        this.context.delete();
    }

    private void writingStart(CommandPool pool, CommandBuffer buffer) {
        pool.reset(this.context.device);
        buffer.beginWriting();
    }

    private void writingStop(CommandBuffer buffer) {
        buffer.endWriting();
    }

    public void render() {
        this.fences[this.currentFrame].fenceWait(this.context.device);

        CommandPool pool = this.commandPools[this.currentFrame];
        CommandBuffer buffer = this.commandBuffers[this.currentFrame];

        this.writingStart(pool, buffer);

        int imageIndex = this.context.swapChain.acquireNextImage(this.context.device, this.presentSemaphores[this.currentFrame]);

        if (this.resize || imageIndex == -1) {
            this.resize();

            return;
        }

        this.render.render(buffer, imageIndex);

        this.writingStop(buffer);

        this.submit(buffer, imageIndex);

        this.resize = this.context.swapChain.presentImage(this.presentQueue, this.renderSemaphores[imageIndex], imageIndex);

        this.currentFrame = (this.currentFrame + 1) % VulkanUtils.MAX_IN_FLIGHT;
    }

    private void resize() {
        if (Window.getWidth() == 0 && Window.getHeight() == 0) {
            return;
        }

        this.resize = true;
        this.context.device.waitIdle();
        this.context.resize();

        for (Semaphore semaphore : this.renderSemaphores) {
            semaphore.delete(this.context.device);
        }

        for (Semaphore semaphore : this.presentSemaphores) {
            semaphore.delete(this.context.device);
        }

        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            this.presentSemaphores[i] = new Semaphore(this.context.device);
        }

        for (int i = 0; i < this.context.swapChain.imageViews.length; i++) {
            this.renderSemaphores[i] = new Semaphore(this.context.device);
        }

        VkExtent2D extent = this.context.swapChain.extent;

        this.projection.resize(extent.width(), extent.height());

        this.render.resize();
    }

    private void submit(CommandBuffer buffer, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            Fence fence = this.fences[this.currentFrame];

            fence.reset(this.context.device);

            VkCommandBufferSubmitInfo.Buffer commands = VkCommandBufferSubmitInfo.calloc(1, stack)
                .sType$Default()
                .commandBuffer(buffer.buffer);
            VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                .sType$Default()
                .stageMask(VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                .semaphore(this.presentSemaphores[this.currentFrame].semaphore);
            VkSemaphoreSubmitInfo.Buffer signalSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                .sType$Default()
                .stageMask(VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
                .semaphore(this.renderSemaphores[imageIndex].semaphore);

            this.graphicsQueue.submit(commands, waitSemaphores, signalSemaphores, this.fences[this.currentFrame]);
        }
    }
}