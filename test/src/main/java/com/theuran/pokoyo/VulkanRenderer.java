package com.theuran.pokoyo;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.allocation.UploadContext;
import com.theuran.pokoyo.vulkan.command.Queue;
import com.theuran.pokoyo.vulkan.frame.Framebuffer;
import com.theuran.pokoyo.vulkan.synchronization.Semaphore;
import mchorse.bbs.core.IDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkCommandBufferSubmitInfo;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

public class VulkanRenderer implements IDisposable {
    public final Queue.GraphicsQueue graphicsQueue;
    private final Queue.PresentQueue presentQueue;

    private final Framebuffer[] frames;

    private final Render render;
    private final VulkanContext context = new VulkanContext();
    private int currentFrame = 0;
    private boolean resize = false;

    public UploadContext upload;
    public VulkanEngine engine;

    public VulkanRenderer(VulkanEngine engine) {
        this.engine = engine;

        this.graphicsQueue = new Queue.GraphicsQueue(this.context, 0);
        this.presentQueue = new Queue.PresentQueue(this.context, 0);

        this.upload = new UploadContext(this.context, this.graphicsQueue.index);

        this.frames = new Framebuffer[VulkanUtils.MAX_IN_FLIGHT];

        for (int i = 0; i < VulkanUtils.MAX_IN_FLIGHT; i++) {
            this.frames[i] = new Framebuffer(this.context, this.graphicsQueue.index);
        }

        this.render = new Render(this.context, this);
    }

    @Override
    public void delete() {
        this.context.device.waitForIdle();

        this.upload.delete(this.context);
        this.render.delete();

        for (Framebuffer frame : this.frames) {
            frame.presentSemaphore.delete(this.context);
            frame.fence.delete(this.context);
            frame.commandBuffer.delete(this.context, frame.commandPool);
            frame.commandPool.delete(this.context);
        }

        this.context.delete();
    }

    public void render() {
        Framebuffer frame = this.frames[this.currentFrame];

        frame.waitForFence(this.context);

        int imageIndex = this.context.swapChain.acquireNextImage(this.context.device, frame.presentSemaphore);

        if (this.resize || imageIndex == -1) {
            this.resize();

            return;
        }

        frame.startWriting(this.context);

        this.render.render(frame.commandBuffer, imageIndex);

        frame.stopWriting();

        this.submit(frame, imageIndex);

        this.resize = this.context.swapChain.presentImage(this.presentQueue, this.context.swapChain.renderSemaphores[imageIndex], imageIndex);
        this.currentFrame = (this.currentFrame + 1) % VulkanUtils.MAX_IN_FLIGHT;
    }

    public void resize() {
        this.resize = true;
        this.context.device.waitForIdle();
        this.context.resize();

        for (Framebuffer frame : this.frames) {
            frame.presentSemaphore.delete(this.context);
            frame.presentSemaphore = new Semaphore(this.context);
        }

        VkExtent2D extent = this.context.swapChain.extent;

        this.engine.cameraController.resize(extent.width(), extent.height());

        this.render.resize();
    }

    private void submit(Framebuffer frame, int imageIndex) {
        frame.resetFence(this.context);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferSubmitInfo.Buffer commands = VkCommandBufferSubmitInfo.calloc(1, stack)
                .sType$Default()
                .commandBuffer(frame.commandBuffer.buffer);
            VkSemaphoreSubmitInfo.Buffer waitSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                .sType$Default()
                .stageMask(VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT)
                .semaphore(frame.presentSemaphore.semaphore);
            VkSemaphoreSubmitInfo.Buffer signalSemaphores = VkSemaphoreSubmitInfo.calloc(1, stack)
                .sType$Default()
                .stageMask(VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT)
                .semaphore(this.context.swapChain.renderSemaphores[imageIndex].semaphore);

            this.graphicsQueue.submit(commands, waitSemaphores, signalSemaphores, frame.fence);
        }
    }
}