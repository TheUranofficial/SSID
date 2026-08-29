package com.theuran.pokoyo.vulkan.device;

import com.theuran.pokoyo.vulkan.VulkanUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Device {
    public final VkDevice device;

    public Device(PhysicalDevice device) {
        IO.println("Creating logical device: " + device.getName());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer requiredExtensions = this.createRequiredExtensions(device, stack);
            VkDeviceQueueCreateInfo.Buffer queueInfo = this.createQueueInfo(device, stack);

            VkPhysicalDeviceVulkan13Features features = VkPhysicalDeviceVulkan13Features.calloc(stack)
                .sType$Default()
                .dynamicRendering(true)
                .synchronization2(true);
            VkPhysicalDeviceFeatures2 features2 = VkPhysicalDeviceFeatures2.calloc(stack)
                .sType$Default()
                .pNext(features.address());

            VkDeviceCreateInfo info = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
                .pNext(features2.address())
                .ppEnabledExtensionNames(requiredExtensions)
                .pQueueCreateInfos(queueInfo);

            PointerBuffer buffer = stack.mallocPointer(1);

            VulkanUtils.checkError(VK10.vkCreateDevice(device.device, info, null, buffer), "Failed to create logical device");

            this.device = new VkDevice(buffer.get(0), device.device, info);
        }
    }

    private PointerBuffer createRequiredExtensions(PhysicalDevice device, MemoryStack stack) {
        Set<String> deviceExtensions = this.getDeviceExtensions(device);
        List<ByteBuffer> extensions = new ArrayList<>();

        for (String extension : PhysicalDevice.REQUIRED_EXTENSIONS) {
            extensions.add(stack.ASCII(extension));
        }

        if (deviceExtensions.contains(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME) && Platform.get() == Platform.MACOSX) {
            extensions.add(stack.ASCII(KHRPortabilitySubset.VK_KHR_PORTABILITY_SUBSET_EXTENSION_NAME));
        }

        IO.println("Enabling device extensions: " + extensions);

        PointerBuffer requiredExtensions = stack.mallocPointer(extensions.size());

        for (ByteBuffer extension : extensions) {
            requiredExtensions.put(extension);
        }

        requiredExtensions.flip();

        return requiredExtensions;
    }

    private Set<String> getDeviceExtensions(PhysicalDevice device) {
        Set<String> extensions = new HashSet<>();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer countBuffer = stack.callocInt(1);

            VK10.vkEnumerateDeviceExtensionProperties(device.device, (String) null, countBuffer, null);

            int count = countBuffer.get(0);

            try (VkExtensionProperties.Buffer properties = VkExtensionProperties.calloc(count)) {
                VK10.vkEnumerateDeviceExtensionProperties(device.device, (String) null, countBuffer, properties);

                for (int i = 0; i < count; i++) {
                    extensions.add(properties.get(i).extensionNameString());
                }
            }
        }

        return extensions;
    }

    private VkDeviceQueueCreateInfo.Buffer createQueueInfo(PhysicalDevice physicalDevice, MemoryStack stack) {
        VkQueueFamilyProperties.Buffer familyProps = physicalDevice.familyProperties;
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

    public void delete() {
        VK10.vkDestroyDevice(this.device, null);
    }

    public void waitIdle() {
        VK10.vkDeviceWaitIdle(this.device);
    }
}