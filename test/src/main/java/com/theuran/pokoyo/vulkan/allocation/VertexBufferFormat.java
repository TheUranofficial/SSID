package com.theuran.pokoyo.vulkan.allocation;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class VertexBufferFormat {
    public final VkPipelineVertexInputStateCreateInfo info;
    private final VkVertexInputAttributeDescription.Buffer attributes;
    private final VkVertexInputBindingDescription.Buffer bindings;

    public VertexBufferFormat() {
        this.attributes = VkVertexInputAttributeDescription.calloc(1);
        this.attributes.get(0)
            .binding(0)
            .location(0)
            .format(VK10.VK_FORMAT_R32G32B32_SFLOAT)
            .offset();

        this.bindings = VkVertexInputBindingDescription.calloc(1);
        this.bindings.get(0)
            .binding(0)
            .stride(3 * 4)
            .inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);

        this.info = VkPipelineVertexInputStateCreateInfo.calloc()
            .sType$Default()
            .pVertexBindingDescriptions(this.bindings)
            .pVertexAttributeDescriptions(this.attributes);
    }

    public void delete() {
        this.attributes.free();
        this.bindings.free();
    }
}