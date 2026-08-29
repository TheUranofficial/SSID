package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.shader.Shader;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;

public class PipelineInfo {
    public final Shader[] shaders;
    public final VkPipelineVertexInputStateCreateInfo info;
    public final int colorFormat;

    public PipelineInfo(Shader[] shaders, VkPipelineVertexInputStateCreateInfo info, int colorFormat) {
        this.shaders = shaders;
        this.info = info;
        this.colorFormat = colorFormat;
    }
}
