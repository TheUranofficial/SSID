package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkImageCreateInfo;

import java.nio.LongBuffer;

public class Image implements IVulkanDisposable {
    private final int mipLevels;
    public final int format;
    public final long image;
    private final long allocation;

    public Image(VulkanContext context, Data data) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.format = data.format;
            this.mipLevels = data.mipLevels;

            VkImageCreateInfo info = VkImageCreateInfo.calloc(stack)
                .sType$Default()
                .imageType(VK10.VK_IMAGE_TYPE_2D)
                .format(this.format)
                .extent(extent -> extent
                    .width(data.width)
                    .height(data.height)
                    .depth(1))
                .mipLevels(this.mipLevels)
                .arrayLayers(data.arrayLayers)
                .samples(data.sampleCount)
                .initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                .tiling(VK10.VK_IMAGE_TILING_OPTIMAL)
                .usage(data.usage);

            VmaAllocationCreateInfo allocationInfo = VmaAllocationCreateInfo.calloc(1, stack)
                .get(0)
                .usage(Vma.VMA_MEMORY_USAGE_AUTO)
                .flags(data.memoryUsage)
                .priority(1);


            PointerBuffer allocation = stack.callocPointer(1);
            LongBuffer buffer = stack.mallocLong(1);

            context.createImage(info, allocationInfo, buffer, allocation);

            this.image = buffer.get(0);
            this.allocation = allocation.get(0);
        }
    }

    @Override
    public void delete(VulkanContext context) {
        Vma.vmaDestroyImage(context.allocator.allocator, this.image, this.allocation);
    }

    public static class Data {
        private int arrayLayers = 1;
        private int format = VK10.VK_FORMAT_R8G8B8A8_SRGB;
        private int height;
        private int mipLevels = 1;
        private int sampleCount = 1;
        private int usage;
        private int memoryUsage = 0;
        private int width;

        public Data arrayLayers(int arrayLayers) {
            this.arrayLayers = arrayLayers;

            return this;
        }

        public Data format(int format) {
            this.format = format;

            return this;
        }

        public Data height(int height) {
            this.height = height;

            return this;
        }

        public Data mipLevels(int mipLevels) {
            this.mipLevels = mipLevels;

            return this;
        }

        public Data sampleCount(int sampleCount) {
            this.sampleCount = sampleCount;

            return this;
        }

        public Data usage(int usage) {
            this.usage = usage;

            return this;
        }

        public Data memoryUsage(int usage) {
            this.memoryUsage = usage;

            return this;
        }

        public Data width(int width) {
            this.width = width;

            return this;
        }
    }
}