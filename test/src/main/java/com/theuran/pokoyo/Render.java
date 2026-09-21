package com.theuran.pokoyo;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.allocation.Buffer;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import com.theuran.pokoyo.vulkan.imageBuffer.Attachment;
import com.theuran.pokoyo.vulkan.pipeline.Pipeline;
import com.theuran.pokoyo.vulkan.pipeline.PipelineInfo;
import com.theuran.pokoyo.vulkan.shader.PushConstantRange;
import com.theuran.pokoyo.vulkan.shader.Shader;
import com.theuran.pokoyo.vulkan.vertex.BufferBuilder;
import com.theuran.pokoyo.vulkan.vertex.VertexBufferFormat;
import mchorse.bbs.core.IDisposable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;

public class Render implements IDisposable {
    private final VkClearValue clearColor;
    private final VkClearValue clearDepth;
    private final VulkanContext context;
    private final VulkanRenderer renderer;
    private VkRenderingAttachmentInfo.Buffer[] attachmentInfo;
    private VkRenderingInfo[] renderInfo;

    private VertexBufferFormat vertexFormat;
    private Shader[] shaders;
    private Pipeline pipeline;
    private Buffer vertexBuffer;
    private ByteBuffer pushConstant;
    private Attachment[] attachmentDepth;
    private VkRenderingAttachmentInfo.Buffer[] attachmentInfoColor;
    private VkRenderingAttachmentInfo[] attachmentInfoDepth;

    public Render(VulkanContext context, VulkanRenderer renderer) {
        this.context = context;
        this.renderer = renderer;
        this.clearColor = VkClearValue.calloc().color(value -> value.float32(0, 0.53f).float32(1, 0.81f).float32(2, 0.92f).float32(3, 1.0f));
        this.clearDepth = VkClearValue.calloc().color(value -> value.float32(0, 1));
        this.attachmentInfo = this.createAttachmentsInfos();
        this.attachmentDepth = this.createDepthAttachments();
        this.attachmentInfoColor = this.createAttachmentsInfos();
        this.attachmentInfoDepth = this.createDepthAttachmentsInfo();
        this.renderInfo = this.createRenderInfo();
        this.vertexFormat = new VertexBufferFormat(true, false);
        this.shaders = this.createShaders();
        this.pushConstant = MemoryUtil.memAlloc(Matrix4f.BYTES * 2);
        this.pipeline = this.createPipeline();
        this.vertexBuffer = this.createVertexBuffer();
    }

    private Attachment[] createDepthAttachments() {
        Attachment[] depthAttachment = new Attachment[this.context.swapChain.imageViews.length];

        for (int i = 0; i < this.context.swapChain.imageViews.length; i++) {
            depthAttachment[i] = new Attachment(this.context.allocator, this.context.device, this.context.swapChain.extent.width(), this.context.swapChain.extent.height(), VK10.VK_FORMAT_D16_UNORM, VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT);
        }

        return depthAttachment;
    }

    private VkRenderingAttachmentInfo[] createDepthAttachmentsInfo() {
        VkRenderingAttachmentInfo[] result = new VkRenderingAttachmentInfo[this.context.swapChain.imageViews.length];

        for (int i = 0; i < this.context.swapChain.imageViews.length; ++i) {
            result[i] = VkRenderingAttachmentInfo.calloc()
                .sType$Default()
                .imageView(this.attachmentDepth[i].imageView.imageView)
                .imageLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .clearValue(this.clearDepth);
        }

        return result;
    }

    private Pipeline createPipeline() {
        PipelineInfo info = new PipelineInfo(this.shaders, this.vertexFormat.info, this.context.surface.format.imageFormat);

        info.depthFormat = VK10.VK_FORMAT_D16_UNORM;
        info.ranges = new PushConstantRange[] {new PushConstantRange(VK10.VK_SHADER_STAGE_VERTEX_BIT, 0, Matrix4f.BYTES * 2)};

        return new Pipeline(this.context.device, this.context.pipelineCache, info);
    }

    private VkRenderingInfo[] createRenderInfo() {
        VkRenderingInfo[] infos = new VkRenderingInfo[this.context.swapChain.imageViews.length];

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRect2D renderArea = VkRect2D.calloc(stack).extent(this.context.swapChain.extent);

            for (int i = 0; i < this.context.swapChain.imageViews.length; i++) {
                VkRenderingInfo renderingInfo = VkRenderingInfo.calloc()
                    .sType$Default()
                    .renderArea(renderArea)
                    .layerCount(1)
                    .pColorAttachments(this.attachmentInfo[i])
                    .pDepthAttachment(this.attachmentInfoDepth[i]);

                infos[i] = renderingInfo;
            }
        }

        return infos;
    }

    private VkRenderingAttachmentInfo.Buffer[] createAttachmentsInfos() {
        VkRenderingAttachmentInfo.Buffer[] infos = new VkRenderingAttachmentInfo.Buffer[this.context.swapChain.imageViews.length];

        for (int i = 0; i < this.context.swapChain.imageViews.length; i++) {
            VkRenderingAttachmentInfo.Buffer attachments = VkRenderingAttachmentInfo.calloc(1)
                .sType$Default()
                .imageView(this.context.swapChain.imageViews[i].imageView)
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
        BufferBuilder builder = new BufferBuilder();

        builder.begin();

        builder.pos(-0.5f, -0.5f, 0).color(1, 0, 0, 1);
        builder.pos(-0.5f, 0.5f, 0).color(0, 1, 0, 1);
        builder.pos(0.5f, 0.5f, 0).color(0, 0, 1, 1);

        builder.pos(-0.5f, -0.5f, 0).color(1, 0, 0, 1);
        builder.pos(0.5f, 0.5f, 0).color(0, 0, 1, 1);
        builder.pos(0.5f, -0.5f, 0).color(0, 1, 0, 1);

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
            long swapChainImage = this.context.swapChain.imageViews[imageIndex].image;

            VulkanUtils.imageBarrier(stack, buffer.buffer, swapChainImage, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_ACCESS_2_NONE, VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
            VulkanUtils.imageBarrier(stack, buffer.buffer, this.attachmentDepth[imageIndex].imageView.image, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK12.VK_IMAGE_LAYOUT_DEPTH_ATTACHMENT_OPTIMAL, VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK13.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT, VK13.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT | VK13.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT, VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT, VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK13.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT, VK10.VK_IMAGE_ASPECT_DEPTH_BIT);

            VK13.vkCmdBeginRendering(buffer.buffer, this.renderInfo[imageIndex]);
            VK10.vkCmdBindPipeline(buffer.buffer, VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, this.pipeline.pipeline);

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

            this.setPushConstants(buffer.buffer, this.renderer.projection.projectionMatrix, new Matrix4f());

            VK10.vkCmdBindVertexBuffers(buffer.buffer, 0, stack.longs(this.vertexBuffer.buffer), stack.longs(0));
            VK10.vkCmdDraw(buffer.buffer, 6, 1, 0, 0);

            VK13.vkCmdEndRendering(buffer.buffer);

            VulkanUtils.imageBarrier(stack, buffer.buffer, swapChainImage, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT, VK13.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT | VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK13.VK_PIPELINE_STAGE_2_NONE, VK10.VK_IMAGE_ASPECT_COLOR_BIT);
        }
    }

    private void setPushConstants(VkCommandBuffer buffer, Matrix4f projMatrix, Matrix4f modelMatrix) {
        projMatrix.get(this.pushConstant);
        modelMatrix.get(Matrix4f.BYTES, this.pushConstant);
        VK10.vkCmdPushConstants(buffer, this.pipeline.layout, VK10.VK_SHADER_STAGE_VERTEX_BIT, 0, this.pushConstant);
    }

    public void resize() {
        for (VkRenderingInfo info : this.renderInfo) {
            info.free();
        }

        for (VkRenderingAttachmentInfo info : this.attachmentInfoDepth) {
            info.free();
        }

        for (VkRenderingAttachmentInfo.Buffer info : this.attachmentInfoColor) {
            info.free();
        }

        for (Attachment attachment : this.attachmentDepth) {
            attachment.delete(this.context.allocator, this.context.device);
        }

        this.attachmentDepth = this.createDepthAttachments();
        this.attachmentInfo = this.createAttachmentsInfos();
        this.attachmentInfoColor = this.createAttachmentsInfos();
        this.attachmentInfoDepth = this.createDepthAttachmentsInfo();
        this.renderInfo = this.createRenderInfo();
    }

    @Override
    public void delete() {
        for (VkRenderingInfo info : this.renderInfo) {
            info.free();
        }

        for (VkRenderingAttachmentInfo.Buffer info : this.attachmentInfo) {
            info.free();
        }

        for (VkRenderingAttachmentInfo.Buffer info : this.attachmentInfoColor) {
            info.free();
        }

        for (VkRenderingAttachmentInfo info : this.attachmentInfoDepth) {
            info.free();
        }

        for (Attachment attachment : this.attachmentDepth) {
            attachment.delete(this.context.allocator, this.context.device);
        }

        MemoryUtil.memFree(this.pushConstant);

        this.clearColor.free();
        this.clearDepth.free();

        this.vertexBuffer.delete(this.context.allocator);
        this.pipeline.delete(this.context.device);

        for (Shader shader : this.shaders) {
            shader.delete(this.context.device);
        }

        this.vertexFormat.delete();
    }
}