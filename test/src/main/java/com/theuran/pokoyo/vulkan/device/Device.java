package com.theuran.pokoyo.vulkan.device;

import com.theuran.pokoyo.vulkan.VulkanContext;
import mchorse.bbs.core.IDisposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class Device implements IDisposable {
    public final VkDevice device;

    public Device(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer requiredExtensions = this.createRequiredExtensions(context, stack);
            VkDeviceQueueCreateInfo.Buffer queueInfo = this.createQueueInfo(context, stack);

            VkPhysicalDeviceVulkan13Features features13 = VkPhysicalDeviceVulkan13Features.calloc(stack)
                .sType$Default()
                .dynamicRendering(true)
                .synchronization2(true);
            VkPhysicalDeviceVulkan12Features features12 = VkPhysicalDeviceVulkan12Features.calloc(stack)
                .sType$Default()
                .bufferDeviceAddress(true)
                .pNext(features13.address());
            VkPhysicalDeviceVulkan11Features features11 = VkPhysicalDeviceVulkan11Features.calloc(stack)
                .sType$Default()
                .shaderDrawParameters(true)
                .pNext(features12.address());
            VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc(stack)
                .sType$Default()
                .pNext(features11.address());

            features2.features().shaderInt64(true);

            VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
                .pNext(features2.address())
                .ppEnabledExtensionNames(requiredExtensions)
                .pQueueCreateInfos(queueInfo);

            this.device = context.createDevice(info);
        }
    }

    private PointerBuffer createRequiredExtensions(VulkanContext context, MemoryStack stack) {
        Set<String> deviceExtensions = this.getDeviceExtensions(context);
        List<ByteBuffer> extensions = new ArrayList<>();

        for (String extension : PhysicalDevice.REQUIRED_EXTENSIONS) {
            extensions.add(stack.ASCII(extension));
        }

        if (deviceExtensions.contains(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME) && Platform.get() == Platform.MACOSX) {
            extensions.add(stack.ASCII(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
        }

        StringJoiner joiner = new StringJoiner(", ");

        for (ByteBuffer extension : extensions) {
            joiner.add(MemoryUtil.memUTF8(extension));
        }

        IO.println("Enabling device extensions: " + joiner);

        PointerBuffer requiredExtensions = stack.mallocPointer(extensions.size());

        for (ByteBuffer extension : extensions) {
            requiredExtensions.put(extension);
        }

        requiredExtensions.flip();

        return requiredExtensions;
    }

    private Set<String> getDeviceExtensions(VulkanContext context) {
        Set<String> extensions = new HashSet<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer countBuffer = stack.callocInt(1);

            VK10.vkEnumerateDeviceExtensionProperties(context.physicalDevice.device, (String) null, countBuffer, null);

            int count = countBuffer.get(0);

            try (VkExtensionProperties.Buffer properties = VkExtensionProperties.calloc(count)) {
                VK10.vkEnumerateDeviceExtensionProperties(context.physicalDevice.device, (String) null, countBuffer, properties);

                for (int i = 0; i < count; i++) {
                    extensions.add(properties.get(i).extensionNameString());
                }
            }
        }

        return extensions;
    }

    private VkDeviceQueueCreateInfo.Buffer createQueueInfo(VulkanContext context, MemoryStack stack) {
        VkQueueFamilyProperties.Buffer familyProps = context.physicalDevice.familyProperties;
        int familyCount = familyProps != null ? familyProps.capacity() : 0;
        VkDeviceQueueCreateInfo.Buffer infos = VkDeviceQueueCreateInfo.calloc(familyCount, stack);

        for (int i = 0; i < familyCount; i++) {
            int queueCount = familyProps.get(i).queueCount();
            FloatBuffer priorities = stack.callocFloat(queueCount);

            infos.get(i)
                .sType$Default()
                .queueFamilyIndex(i)
                .pQueuePriorities(priorities);
        }

        return infos;
    }

    @Override
    public void delete() {
        VK10.vkDestroyDevice(this.device, null);
    }

    public void waitForIdle() {
        VK10.vkDeviceWaitIdle(this.device);
    }
}