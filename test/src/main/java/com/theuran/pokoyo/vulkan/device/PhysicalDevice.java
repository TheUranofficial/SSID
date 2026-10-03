package com.theuran.pokoyo.vulkan.device;

import com.theuran.pokoyo.vulkan.VulkanContext;
import com.theuran.pokoyo.vulkan.utils.VulkanException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhysicalDevice {
    public static final Set<String> REQUIRED_EXTENSIONS = new HashSet<>(List.of(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));

    public final VkExtensionProperties.Buffer extensions;
    public final VkPhysicalDeviceMemoryProperties memoryProperties;
    public final VkPhysicalDevice device;
    public final VkPhysicalDeviceFeatures features;
    public final VkPhysicalDeviceProperties2 properties;
    public final VkQueueFamilyProperties.Buffer familyProperties;

    public PhysicalDevice(VkPhysicalDevice device) {
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer sluttyBuffer = stack.mallocInt(1);
            int error;

            this.properties = VkPhysicalDeviceProperties2.calloc().sType$Default();
            VK11.vkGetPhysicalDeviceProperties2(this.device, this.properties);

            error = VK10.vkEnumerateDeviceExtensionProperties(this.device, (String) null, sluttyBuffer, null);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get number of device extension", error);
            }

            this.extensions = VkExtensionProperties.calloc(sluttyBuffer.get(0));

            error = VK10.vkEnumerateDeviceExtensionProperties(this.device, (String) null, sluttyBuffer, this.extensions);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get extension properties", error);
            }

            VK10.vkGetPhysicalDeviceQueueFamilyProperties(this.device, sluttyBuffer, null);
            this.familyProperties = VkQueueFamilyProperties.calloc(sluttyBuffer.get(0));
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(this.device, sluttyBuffer, this.familyProperties);

            this.features = VkPhysicalDeviceFeatures.calloc();
            VK10.vkGetPhysicalDeviceFeatures(this.device, this.features);

            this.memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
            VK10.vkGetPhysicalDeviceMemoryProperties(this.device, this.memoryProperties);
        }
    }

    public void delete() {
        this.memoryProperties.free();
        this.properties.free();
        this.familyProperties.free();
        this.extensions.free();
        this.features.free();
    }

    public String getName() {
        return this.properties.properties().deviceNameString();
    }

    private boolean hasGraphicsFamily() {
        boolean result = false;
        int count = this.familyProperties != null ? this.familyProperties.capacity() : 0;

        for (int i = 0; i < count; i++) {
            VkQueueFamilyProperties familyProps = this.familyProperties.get(i);

            if ((familyProps.queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
                result = true;

                break;
            }
        }

        return result;
    }

    public boolean supportsExtensions(Set<String> extensions) {
        Set<String> copyExtensions = new HashSet<>(extensions);
        int numExtensions = this.extensions != null ? this.extensions.capacity() : 0;

        for (int i = 0; i < numExtensions; i++) {
            String extensionName = this.extensions.get(i).extensionNameString();

            copyExtensions.remove(extensionName);
        }

        boolean result = copyExtensions.isEmpty();

        if (!result) {
            IO.println("At least " + copyExtensions.iterator().next() + " extension is not supported by device " + this.getName());
        }

        return result;
    }

    public static PhysicalDevice createPhysicalDevice(VulkanContext context, String name) {
        PointerBuffer pointerDevices = getPhysicalDevices(context);
        int count = pointerDevices.capacity();
        List<PhysicalDevice> physicalDevices = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            VkPhysicalDevice vulkanDevice = new VkPhysicalDevice(pointerDevices.get(i), context.instance.instance);
            PhysicalDevice physicalDevice = new PhysicalDevice(vulkanDevice);

            if (!physicalDevice.hasGraphicsFamily()) {
                IO.println("Device " + physicalDevice.getName() + " does not support graphics queue family");
                physicalDevice.delete();

                continue;
            }

            if (!physicalDevice.supportsExtensions(REQUIRED_EXTENSIONS)) {
                IO.println("Device " + physicalDevice.getName() + " does not support required extensions");
                physicalDevice.delete();

                continue;
            }

            if (name != null && name.equals(physicalDevice.getName())) {
                for (PhysicalDevice device : physicalDevices) {
                    device.delete();
                }

                IO.println("Selected preferred device: " + physicalDevice.getName());

                return physicalDevice;
            }

            physicalDevices.add(physicalDevice);
        }

        if (physicalDevices.isEmpty()) {
            throw new RuntimeException("No suitable physical devices found");
        }

        PhysicalDevice selected = physicalDevices.getFirst();

        for (PhysicalDevice device : physicalDevices) {
            if (device.properties.properties().deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                selected = device;

                break;
            }
        }

        for (PhysicalDevice device : physicalDevices) {
            if (device != selected) {
                device.delete();
            }
        }

        IO.println("Selected device: " + selected.getName());

        return selected;
    }

    private static PointerBuffer getPhysicalDevices(VulkanContext context) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer sluttyBuffer = stack.mallocInt(1);
            int error = VK10.vkEnumeratePhysicalDevices(context.instance.instance, sluttyBuffer, null);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get number of physical devices", error);
            }

            int numDevices = sluttyBuffer.get(0);

            IO.println("Detected count of physical devices " + numDevices);

            PointerBuffer devices = stack.mallocPointer(numDevices);

            error = VK10.vkEnumeratePhysicalDevices(context.instance.instance, sluttyBuffer, devices);

            if (error != VK10.VK_SUCCESS) {
                throw new VulkanException("Failed to get physical devices", error);
            }

            return devices;
        }
    }
}