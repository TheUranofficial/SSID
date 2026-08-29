package com.theuran.pokoyo.vulkan.shader;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import mchorse.bbs.utils.IOUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class Shader {
    public final long shader;
    public final int stage;

    public Shader(Device device, int stage, String path) {
        this.stage = stage;

        try {
            this.shader = this.createShader(device, IOUtils.readBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Error reading shader: " + e);
        }
    }

    private long createShader(Device device, byte[] bytes) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer code = stack.malloc(bytes.length).put(0, bytes);

            VkShaderModuleCreateInfo info = VkShaderModuleCreateInfo.calloc(stack)
                .sType$Default()
                .pCode(code);

            LongBuffer buffer = stack.mallocLong(1);

            VulkanUtils.checkError(VK10.vkCreateShaderModule(device.device, info, null, buffer), "Failed to create shader");

            return buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroyShaderModule(device.device, this.shader, null);
    }
}