package com.theuran.pokoyo;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.command.CommandBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class Render {
    private final VkClearValue clearColor;
    private VkRenderingAttachmentInfo.Buffer[] attachmentInfo;
    private VkRenderingInfo[] renderInfo;

    public Render(VulkanContext context) {
        this.clearColor = VkClearValue.calloc().color(value -> value.float32(0, 0.53f).float32(1, 0.81f).float32(2, 0.92f).float32(3, 1.0f));
        this.attachmentInfo = this.createAttachmentsInfos(context);
        this.renderInfo = this.createRenderInfo(context);
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

    public void render(VulkanContext context, CommandBuffer buffer, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long swapChainImage = context.swapChain.imageViews[imageIndex].image;

            VulkanUtils.imageBarrier(stack, buffer.buffer, swapChainImage, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT, VK13.VK_ACCESS_2_NONE, VK13.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT, VK10.VK_IMAGE_ASPECT_COLOR_BIT);

            VK13.vkCmdBeginRendering(buffer.buffer, this.renderInfo[imageIndex]);

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
    }
}