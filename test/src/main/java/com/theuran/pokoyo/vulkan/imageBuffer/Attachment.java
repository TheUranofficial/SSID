package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.IVulkanDisposable;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;

public class Attachment implements IVulkanDisposable {
    private final Image image;
    public final ImageView imageView;
    private boolean depth;

    public Attachment(VulkanContext context, int width, int height, int format, int usage) {
        this.image = new Image(context, new Image.Data().width(width).height(height).usage(usage | VK10.VK_IMAGE_USAGE_SAMPLED_BIT).format(format).memoryUsage(Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT));

        int aspectMask = 0;

        if ((usage & VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) > 0) {
            aspectMask = VK10.VK_IMAGE_ASPECT_COLOR_BIT;
            this.depth = false;
        }

        if ((usage & VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT) > 0) {
            aspectMask = VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
            this.depth = true;
        }

        this.imageView = new ImageView(context, this.image.image, new ImageView.Data().format(this.image.format).aspectMask(aspectMask));
    }

    @Override
    public void delete(VulkanContext context) {
        this.image.delete(context);
        this.imageView.delete(context);
    }
}