package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.shader.PushConstantRange;
import com.theuran.pokoyo.vulkan.shader.Shader;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class PipelineInfo {
    public final Shader[] shaders;
    public final VkPipelineVertexInputStateCreateInfo info;
    public final int colorFormat;
    public int depthFormat;
    public PushConstantRange[] ranges;

    public PipelineInfo(Shader[] shaders, VkPipelineVertexInputStateCreateInfo info, int colorFormat) {
        this.shaders = shaders;
        this.info = info;
        this.colorFormat = colorFormat;
        this.depthFormat = VK10.VK_FORMAT_UNDEFINED;
    }
}
