package com.theuran.pokoyo;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.allocation.Buffer;
import com.theuran.pokoyo.vulkan.allocation.VertexBufferFormat;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import com.theuran.pokoyo.vulkan.command.CommandPool;
import com.theuran.pokoyo.vulkan.device.Device;
import com.theuran.pokoyo.vulkan.pipeline.Pipeline;
import com.theuran.pokoyo.vulkan.pipeline.PipelineInfo;
import com.theuran.pokoyo.vulkan.shader.Shader;
import com.theuran.pokoyo.vulkan.vertex.BufferBuilder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class Render {
    private final VkClearValue clearColor;
    private final VulkanContext context;
    private final VulkanRenderer renderer;
    private VkRenderingAttachmentInfo.Buffer[] attachmentInfo;
    private VkRenderingInfo[] renderInfo;

    private VertexBufferFormat vertexFormat;
    private Shader[] shaders;
    private Pipeline pipeline;
    private Buffer vertexBuffer;

    public Render(VulkanContext context, VulkanRenderer renderer) {
        this.context = context;
        this.renderer = renderer;
        this.clearColor = VkClearValue.calloc().color(value -> value.float32(0, 0.53f).float32(1, 0.81f).float32(2, 0.92f).float32(3, 1.0f));
        this.attachmentInfo = this.createAttachmentsInfos(context);
        this.renderInfo = this.createRenderInfo(context);
        this.vertexFormat = new VertexBufferFormat();
        this.shaders = this.createShaders();
        this.pipeline = new Pipeline(context.device, context.pipelineCache, new PipelineInfo(this.shaders, this.vertexFormat.info, context.surface.format.imageFormat));
        this.vertexBuffer = this.createVertexBuffer();
    }

    private VkRenderingInfo[] createRenderInfo(VulkanContext context) {
        VkRenderingInfo[] infos = new VkRenderingInfo[context.swapChain.imageViews.length];

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRect2D renderArea = VkRect2D.calloc(stack).extent(context.swapChain.extent);

            for (int i = 0; i < context.swapChain.imageViews.length; i++) {
                VkRenderingInfo renderingInfo = VkRenderingInfo.calloc()
                    .sType$Default()
                    .renderArea(renderArea)
                    .layerCount(1)
                    .pColorAttachments(this.attachmentInfo[i]);

                infos[i] = renderingInfo;
            }
        }

        return infos;
    }

    private VkRenderingAttachmentInfo.Buffer[] createAttachmentsInfos(VulkanContext context) {
        VkRenderingAttachmentInfo.Buffer[] infos = new VkRenderingAttachmentInfo.Buffer[context.swapChain.imageViews.length];

        for (int i = 0; i < context.swapChain.imageViews.length; i++) {
            VkRenderingAttachmentInfo.Buffer attachments = VkRenderingAttachmentInfo.calloc(1)
                .sType$Default()
                .imageView(context.swapChain.imageViews[i].imageView)
                .imageLayout(KHRSynchronization2.VK_IMAGE_LAYOUT_ATTACHMENT_OPTIMAL_KHR)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                .clearValue(this.clearColor);

            infos[i] = attachments;
        }

        return infos;
    }

    private Shader[] createShaders() {
        return new Shader[] {
            new Shader(this.context.device, VK10.VK_SHADER_STAGE_VERTEX_BIT, "/pokoyo/shaders/vertex/shader.spv"),
            new Shader(this.context.device, VK10.VK_SHADER_STAGE_FRAGMENT_BIT, "/pokoyo/shaders/fragment/shader.spv"),
        };
    }

    private Buffer createVertexBuffer() {
        BufferBuilder builder = new BufferBuilder(3);

        builder.begin();

        builder.pos(0, -0.5f, 0).endVertex();
        builder.pos(0.5f, 0.5f, 0).endVertex();
        builder.pos(-0.5f, 0.5f, 0).endVertex();

        CommandBuffer buffer = new CommandBuffer(this.context.device, this.renderer.commandPools[0], true, true);

        buffer.beginWriting();

        Buffer vertexBuffer = builder.upload(this.renderer.upload, this.context.allocator, buffer);

        buffer.endWriting();

        buffer.submitAndWait(this.context.device, this.renderer.graphicsQueue);

        builder.free();
        this.renderer.upload.delete(this.context.allocator);

        buffer.delete(this.context.device, this.renderer.commandPools[0]);

        return vertexBuffer;
    }

    public void render(CommandBuffer buffer, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long swapChainImage = context.swapChain.imageViews[imageIndex].image;

            VulkanUtils.imageBarrier(stack, buffer.buffer, swapChainImage, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_ACCESS_2_NONE, VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);

            VK13.vkCmdBeginRendering(buffer.buffer, this.renderInfo[imageIndex]);

            VkExtent2D extent = this.context.swapChain.extent;

            VkViewport.Buffer viewport = VkViewport.calloc(1, stack)
                .x(0)
                .y(0)
                .width(extent.width())
                .height(extent.height())
                .minDepth(0)
                .maxDepth(1);
            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack).extent(extent);

            VK10.vkCmdSetViewport(buffer.buffer, 0, viewport);
            VK10.vkCmdSetScissor(buffer.buffer, 0, scissor);

            VK10.vkCmdBindPipeline(buffer.buffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline.pipeline);
            VK10.vkCmdBindVertexBuffers(buffer.buffer, 0, stack.longs(vertexBuffer.buffer), stack.longs(0));
            VK10.vkCmdDraw(buffer.buffer, 3, 1, 0, 0);

            VK13.vkCmdEndRendering(buffer.buffer);

            VulkanUtils.imageBarrier(stack, buffer.buffer, swapChainImage, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT, VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT | VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK13.VK_PIPELINE_STAGE_2_NONE, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
        }
    }

    public void delete() {
        for (VkRenderingInfo info : this.renderInfo) {
            info.free();
        }

        for (VkRenderingAttachmentInfo.Buffer info : this.attachmentInfo) {
            info.free();
        }

        this.clearColor.free();

        this.vertexBuffer.delete(this.context.allocator);
        this.pipeline.delete(this.context.device);

        for (Shader shader : this.shaders) {
            shader.delete(this.context.device);
        }

        this.vertexFormat.delete();
    }
}