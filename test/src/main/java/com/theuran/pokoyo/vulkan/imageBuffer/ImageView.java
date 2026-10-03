package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public class ImageView implements IVulkanDisposable {
    private final int aspectMask;
    private final int mipLevels;
    public final long image;
    public final long imageView;

    public ImageView(VulkanContext context, long image, Data data) {
        this.aspectMask = data.aspectMask;
        this.mipLevels = data.mipLevels;
        this.image = image;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageViewCreateInfo info = VkImageViewCreateInfo.calloc(stack)
                .sType$Default()
                .image(this.image)
                .viewType(data.viewType)
                .format(data.format)
                .subresourceRange(range -> range
                    .aspectMask(this.aspectMask)
                    .baseMipLevel(0)
                    .levelCount(this.mipLevels)
                    .baseArrayLayer(data.baseArrayLayer)
                    .layerCount(data.layerCount)
                );

            this.imageView = context.createImageView(info);
        }
    }

    @Override
    public void delete(VulkanContext context) {
        VK10.vkDestroyImageView(context.device.device, this.imageView, null);
    }

    public static class Data {
        private int aspectMask;
        private int baseArrayLayer;
        private int format;
        private int layerCount;
        private int mipLevels;
        private int viewType;

        public Data() {
            this.baseArrayLayer = 0;
            this.layerCount = 1;
            this.mipLevels = 1;
            this.viewType = VK10.VK_IMAGE_VIEW_TYPE_2D;
        }

        public Data aspectMask(int aspectMask) {
            this.aspectMask = aspectMask;

            return this;
        }

        public Data baseArrayLayer(int baseArrayLayer) {
            this.baseArrayLayer = baseArrayLayer;
            return this;
        }

        public Data format(int format) {
            this.format = format;

            return this;
        }

        public Data layerCount(int layerCount) {
            this.layerCount = layerCount;

            return this;
        }

        public Data mipLevels(int mipLevels) {
            this.mipLevels = mipLevels;

            return this;
        }

        public Data viewType(int viewType) {
            this.viewType = viewType;

            return this;
        }
    }
}
