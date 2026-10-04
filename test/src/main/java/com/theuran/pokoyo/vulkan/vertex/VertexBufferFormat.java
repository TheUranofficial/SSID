package com.theuran.pokoyo.vulkan.vertex;

import mchorse.bbs.core.IDisposable;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

public class VertexBufferFormat implements IDisposable {
    public final VkPipelineVertexInputStateCreateInfo info;
    private final VkVertexInputAttributeDescription.Buffer attributes;
    private final VkVertexInputBindingDescription.Buffer bindings;

    public final int stride;

    public VertexBufferFormat(boolean color, boolean tex) {
        int attributes = 1;

        if (color) {
            attributes++;
        }

        if (tex) {
            attributes++;
        }

        this.attributes = VkVertexInputAttributeDescription.calloc(attributes);

        int index = 0;
        int offset = 0;

        this.attributes.get(index)
            .binding(0)
            .location(index)
            .format(VK10.VK_FORMAT_R32G32B32_SFLOAT)
            .offset(offset);

        index++;
        offset += 3 * Float.BYTES;

        if (color) {
            this.attributes.get(index)
                .binding(0)
                .location(index)
                .format(VK10.VK_FORMAT_R8G8B8A8_UNORM)
                .offset(offset);

            index++;
            offset += 4 * Byte.BYTES;
        }

        if (tex) {
            this.attributes.get(index)
                .binding(0)
                .location(index)
                .format(VK10.VK_FORMAT_R32G32_SFLOAT)
                .offset(offset);

            index++;
            offset += 2 * Float.BYTES;
        }

        this.stride = offset;

        this.bindings = VkVertexInputBindingDescription.calloc(1);
        this.bindings.get(0)
            .binding(0)
            .stride(offset)
            .inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);

        this.info = VkPipelineVertexInputStateCreateInfo.calloc()
            .sType$Default()
            .pVertexBindingDescriptions(this.bindings)
            .pVertexAttributeDescriptions(this.attributes);
    }

    @Override
    public void delete() {
        this.attributes.free();
        this.bindings.free();
    }
}