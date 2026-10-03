package com.theuran.pokoyo.vulkan.shader;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import mchorse.bbs.utils.IOUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Shader implements IVulkanDisposable {
    public final long shader;
    public final int stage;

    public Shader(VulkanContext context, int stage, String path) {
        this.stage = stage;

        try {
            this.shader = this.createShader(context, IOUtils.readBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Error reading shader: " + e);
        }
    }

    private long createShader(VulkanContext context, byte[] bytes) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer code = stack.malloc(bytes.length).put(0, bytes);

            VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc(stack)
                .sType$Default()
                .pCode(code);

            return context.createShader(info);
        }
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroyShaderModule(context.device.device, this.shader, null);
    }
}