package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.shader.PushConstantRange;
import com.theuran.pokoyo.vulkan.shader.Shader;
import org.lwjgl.vulkan.VK10;

public class PipelineInfo {
    public final Shader[] shaders;
    public final int colorFormat;
    public int depthFormat = VK10.VK_FORMAT_UNDEFINED;
    public PushConstantRange[] ranges;

    public PipelineInfo(Shader[] shaders, int colorFormat) {
        this.shaders = shaders;
        this.colorFormat = colorFormat;
    }
}
