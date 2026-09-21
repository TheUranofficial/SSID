package com.theuran.pokoyo.vulkan.pipeline;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class Pipeline {
    public final long pipeline;
    public final long layout;

    public Pipeline(Device device, PipelineCache cache, PipelineInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer buffer = stack.mallocLong(1);
            ByteBuffer main = stack.UTF8("main");
            VkPipelineShaderStageCreateInfo.Buffer stages = VkPipelineShaderStageCreateInfo.calloc(info.shaders.length, stack);

            for (int i = 0; i < info.shaders.length; i++) {
                stages.get(i)
                    .sType$Default()
                    .stage(info.shaders[i].stage)
                    .module(info.shaders[i].shader)
                    .pName(main);
            }

            VkPipelineInputAssemblyStateCreateInfo assemblyInfo = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType$Default()
                .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            VkPipelineViewportStateCreateInfo viewportInfo = VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default()
                .viewportCount(1)
                .scissorCount(1);
            VkPipelineRasterizationStateCreateInfo rasterizationInfo = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType$Default()
                .polygonMode(VK10.VK_POLYGON_MODE_FILL)
                .cullMode(VK10.VK_CULL_MODE_NONE)
                .frontFace(VK10.VK_FRONT_FACE_CLOCKWISE)
                .lineWidth(1);
            VkPipelineMultisampleStateCreateInfo multisampleInfo = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType$Default()
                .rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT);
            VkPipelineDepthStencilStateCreateInfo depthStencil = null;

            if (info.depthFormat != VK10.VK_FORMAT_UNDEFINED) {
                depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack)
                    .sType$Default()
                    .depthTestEnable(true)
                    .depthWriteEnable(true)
                    .depthCompareOp(VK10.VK_COMPARE_OP_LESS_OR_EQUAL)
                    .depthBoundsTestEnable(false)
                    .stencilTestEnable(false);
            }

            VkPipelineDynamicStateCreateInfo dynamicInfo = VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                .pDynamicStates(stack.ints(
                    VK10.VK_DYNAMIC_STATE_VIEWPORT,
                    VK10.VK_DYNAMIC_STATE_SCISSOR
                ));

            VkPushConstantRange.Buffer pushConstantBuffer = null;
            int pushConstantsCount = info.ranges != null ? info.ranges.length : 0;

            if (pushConstantsCount > 0) {
                pushConstantBuffer = VkPushConstantRange.calloc(pushConstantsCount, stack);

                for (int i = 0; i < pushConstantsCount; i++) {
                    pushConstantBuffer.get(i)
                        .stageFlags(info.ranges[i].stage)
                        .offset(info.ranges[i].offset)
                        .size(info.ranges[i].size);
                }
            }

            VkPipelineColorBlendAttachmentState.Buffer blendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack)
                .colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
                .blendEnable(false);
            VkPipelineColorBlendStateCreateInfo colorBlendInfo = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType$Default()
                .pAttachments(blendAttachment);

            IntBuffer colorFormats = stack.mallocInt(1);

            colorFormats.put(0, info.colorFormat);

            VkPipelineRenderingCreateInfo renderingInfo = VkPipelineRenderingCreateInfo.calloc(stack)
                .sType$Default()
                .colorAttachmentCount(1)
                .pColorAttachmentFormats(colorFormats);

            if (depthStencil != null) {
                renderingInfo.depthAttachmentFormat(info.depthFormat);
            }

            VkPipelineLayoutCreateInfo layoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                .sType$Default()
                .pPushConstantRanges(pushConstantBuffer);

            VulkanUtils.checkError(VK10.vkCreatePipelineLayout(device.device, layoutInfo, null, buffer), "Failed to create pipeline layout");

            this.layout = buffer.get(0);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack)
                .sType$Default()
                .renderPass(VK10.VK_NULL_HANDLE)
                .pStages(stages)
                .pVertexInputState(info.info)
                .pInputAssemblyState(assemblyInfo)
                .pViewportState(viewportInfo)
                .pRasterizationState(rasterizationInfo)
                .pColorBlendState(colorBlendInfo)
                .pMultisampleState(multisampleInfo)
                .pDynamicState(dynamicInfo)
                .layout(this.layout)
                .pNext(renderingInfo);

            if (depthStencil != null) {
                pipelineInfo.pDepthStencilState(depthStencil);
            }

            VulkanUtils.checkError(VK10.vkCreateGraphicsPipelines(device.device, cache.cache, pipelineInfo, null, buffer), "Error creating graphics pipeline");

            this.pipeline = buffer.get(0);
        }
    }

    public void delete(Device device) {
        VK10.vkDestroyPipelineLayout(device.device, this.layout, null);
        VK10.vkDestroyPipeline(device.device, this.pipeline, null);
    }
}