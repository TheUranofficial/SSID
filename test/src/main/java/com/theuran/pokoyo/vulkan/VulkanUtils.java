package com.theuran.pokoyo.vulkan;

import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public class VulkanUtils {
    public static final int MAX_IN_FLIGHT = 2;

    public static void checkError(int result, String message) {
        if (result != VK10.VK_SUCCESS) {
            throw new RuntimeException("[Vulkan] %s [%d]".formatted(message, result));
        }
    }

    public static void imageBarrier(MemoryStack stack, VkCommandBuffer buffer, long image, int oldLayout, int newLayout, long sourceStage, long distanceStage, long sourceAccess, long distanceAccess, int aspectMask) {
        VkImageMemoryBarrier2.Buffer imageBarrier = VkImageMemoryBarrier2.calloc(1, stack)
            .sType$Default()
            .oldLayout(oldLayout)
            .newLayout(newLayout)
            .srcStageMask(sourceStage)
            .dstStageMask(distanceStage)
            .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
            .dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
            .subresourceRange(range -> range
                .aspectMask(aspectMask)
                .baseMipLevel(0)
                .levelCount(VK10.VK_REMAINING_MIP_LEVELS)
                .baseArrayLayer(0)
                .layerCount(VK10.VK_REMAINING_ARRAY_LAYERS)
            )
            .image(image);

        VkDependencyInfo dependency = VkDependencyInfo.calloc(stack)
            .sType$Default()
            .pImageMemoryBarriers(imageBarrier);

        VK13.vkCmdPipelineBarrier2(buffer, dependency);
    }
}