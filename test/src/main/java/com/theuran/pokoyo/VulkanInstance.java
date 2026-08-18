package com.theuran.pokoyo;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VulkanInstance {
    private static final String VALIDATION_LAYER = "VK_LAYER_KHRONOS_validation";
    private static final String PORTABILITY_EXTENSION = "VK_KHR_portability_enumeration";

    public static final int MESSAGE_SEVERITY_BITMASK =
        EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;

    public static final int MESSAGE_TYPE_BITMASK =
        EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;

    private VkDebugUtilsMessengerCreateInfoEXT debugUtils;
    private final long debugMessenger;
    private final VkInstance instance;

    public VulkanInstance(boolean validation) {
        IO.println("Creating Vulkan instance");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            List<String> validationLayers = this.getValidationLayers();
            boolean supportsValidation = validation && !validationLayers.isEmpty();

            if (validation && !supportsValidation) {
                IO.println("Validation layers support is not found");
            }

            Set<String> extensions = this.getInstanceExtensions();
            boolean usePortability = extensions.contains(PORTABILITY_EXTENSION) && Platform.get() == Platform.MACOSX;

            IO.println("Vulkan instance extensions: " + extensions);

            if (supportsValidation) {
                this.debugUtils = this.createDebugCallback();
            }

            VkApplicationInfo appInfo = this.createAppInfo(stack);
            PointerBuffer layersBuffer = this.createLayersBuffer(stack, validationLayers, supportsValidation);
            PointerBuffer extensionsBuffer = this.createExtensionsBuffer(stack, supportsValidation, usePortability);

            this.instance = this.createInstance(stack, appInfo, layersBuffer, extensionsBuffer, usePortability);
            this.debugMessenger = supportsValidation ? this.createDebugMessenger(stack, this.instance, this.debugUtils) : 0;
        }
    }

    public void delete() {
        if (this.debugMessenger != 0) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(this.instance, this.debugMessenger, null);
        }

        if (this.instance != null) {
            VK10.vkDestroyInstance(this.instance, null);
        }

        if (this.debugUtils != null) {
            this.debugUtils.pfnUserCallback().free();
            this.debugUtils.free();
        }
    }

    /* Setup methods */

    private VkApplicationInfo createAppInfo(MemoryStack stack) {
        ByteBuffer name = stack.UTF8("Okak");

        return VkApplicationInfo.malloc(stack)
            .sType$Default()
            .pApplicationName(name)
            .applicationVersion(1)
            .pEngineName(name)
            .engineVersion(0)
            .apiVersion(VK13.VK_API_VERSION_1_3);
    }

    private PointerBuffer createLayersBuffer(MemoryStack stack, List<String> layers, boolean supportsValidation) {
        if (supportsValidation && !layers.isEmpty()) {
            IO.println("Validation layers: " + layers);

            PointerBuffer buffer = stack.mallocPointer(layers.size());

            for (int i = 0; i < layers.size(); i++) {
                IO.println("Using validation layer: " + layers.get(i));
                buffer.put(i, stack.ASCII(layers.get(i)));
            }

            return buffer;
        }

        return null;
    }

    private PointerBuffer createExtensionsBuffer(MemoryStack stack, boolean supportsValidation, boolean usePortability) {
        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();

        if (glfwExtensions == null) {
            throw new RuntimeException("Failed to find the GLFW platform surface extensions");
        }

        List<ByteBuffer> additional = new ArrayList<>();

        if (supportsValidation) {
            additional.add(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
        }

        if (usePortability) {
            additional.add(stack.UTF8(PORTABILITY_EXTENSION));
        }

        PointerBuffer result = stack.mallocPointer(glfwExtensions.remaining() + additional.size());

        result.put(glfwExtensions);

        for (ByteBuffer extension : additional) {
            result.put(extension);
        }

        return result.flip();
    }

    private VkInstance createInstance(MemoryStack stack, VkApplicationInfo appInfo, PointerBuffer layers, PointerBuffer extensions, boolean usePortability) {
        VkInstanceCreateInfo instanceInfo = VkInstanceCreateInfo.calloc(stack)
            .sType$Default()
            .pNext(this.debugUtils != null ? this.debugUtils.address() : 0)
            .pApplicationInfo(appInfo)
            .ppEnabledLayerNames(layers)
            .ppEnabledExtensionNames(extensions);

        if (usePortability) {
            instanceInfo.flags(KHRPortabilityEnumeration.VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
        }

        PointerBuffer instance = stack.mallocPointer(1);

        this.checkError(VK10.vkCreateInstance(instanceInfo, null, instance), "Error creating instance");

        return new VkInstance(instance.get(0), instanceInfo);
    }

    private long createDebugMessenger(MemoryStack stack, VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT debugUtils) {
        LongBuffer buffer = stack.mallocLong(1);

        this.checkError(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, debugUtils, null, buffer), "Error creating debug utils");

        return buffer.get(0);
    }

    private VkDebugUtilsMessengerCreateInfoEXT createDebugCallback() {
        return VkDebugUtilsMessengerCreateInfoEXT.calloc()
            .sType$Default()
            .messageSeverity(MESSAGE_SEVERITY_BITMASK)
            .messageType(MESSAGE_TYPE_BITMASK)
            .pfnUserCallback((_, _, data, _) -> {
                VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(data);

                IO.println("Debug callback: " + callbackData.pMessageString());

                return VK10.VK_FALSE;
            });
    }

    private List<String> getValidationLayers() {
        return this.getLayers().contains(VALIDATION_LAYER) ? List.of(VALIDATION_LAYER) : List.of();
    }

    private List<String> getLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer countBuffer = stack.mallocInt(1);

            VK10.vkEnumerateInstanceLayerProperties(countBuffer, null);

            int count = countBuffer.get(0);

            if (count > 0) {
                VkLayerProperties.Buffer buffer = VkLayerProperties.malloc(count, stack);

                VK10.vkEnumerateInstanceLayerProperties(countBuffer, buffer);

                List<String> layers = new ArrayList<>(count);

                for (VkLayerProperties layer : buffer) {
                    layers.add(layer.layerNameString());
                }

                return layers;
            }

            return List.of();
        }
    }

    private Set<String> getInstanceExtensions() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer countBuffer = stack.mallocInt(1);

            VK10.vkEnumerateInstanceExtensionProperties((String) null, countBuffer, null);

            int count = countBuffer.get(0);

            if (count > 0) {
                VkExtensionProperties.Buffer buffer = VkExtensionProperties.malloc(count, stack);

                VK10.vkEnumerateInstanceExtensionProperties((String) null, countBuffer, buffer);

                Set<String> extensions = new HashSet<>(count);

                for (VkExtensionProperties properties : buffer) {
                    extensions.add(properties.extensionNameString());
                }

                return extensions;
            }

            return Set.of();
        }
    }

    private void checkError(int result, String message) {
        if (result != VK10.VK_SUCCESS) {
            throw new RuntimeException("%s [%d]".formatted(message, result));
        }
    }
}