package com.theuran.pokoyo.vulkan.imageBuffer;

import com.theuran.pokoyo.vulkan.allocation.Allocator;
import com.theuran.pokoyo.vulkan.device.Device;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK10;

public class Attachment {
    private final Image image;
    public final ImageView imageView;
    private boolean depth;

    public Attachment(Allocator allocator, Device device, int width, int height, int format, int usage) {
        this.image = new Image(allocator, new Image.Data().width(width).height(height).usage(usage | VK10.VK_IMAGE_USAGE_SAMPLED_BIT).format(format).memoryUsage(Vma.VMA_ALLOCATION_CREATE_DEDICATED_MEMORY_BIT));

        int aspectMask = 0;

        if ((usage & VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) > 0) {
            aspectMask = VK10.VK_IMAGE_ASPECT_COLOR_BIT;
            this.depth = false;
        }

        if ((usage & VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT) > 0) {
            aspectMask = VK10.VK_IMAGE_ASPECT_DEPTH_BIT;
            this.depth = true;
        }

        this.imageView = new ImageView(device, this.image.image, new ImageView.Data().format(this.image.format).aspectMask(aspectMask));
    }

    public void delete(Allocator allocator, Device device) {
        this.image.delete(allocator);
        this.imageView.delete(device);
    }
}