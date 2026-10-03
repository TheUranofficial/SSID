package com.theuran.pokoyo.vulkan.utils;

import com.theuran.pokoyo.vulkan.VulkanContext;

/**
 * Disposable interface, anything vulkan object that holds any purgable state should
 * implement this interface
 */
public interface IVulkanDisposable {
    /**
     * This method should be responsible for cleaning up resources with vulkan context
     */
    void delete(VulkanContext context);
}