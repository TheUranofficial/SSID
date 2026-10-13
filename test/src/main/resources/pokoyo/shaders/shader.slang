struct Vertex {
    float x;
    float y;
    float z;
    uint color;
};

struct ShaderData {
    float4x4 projection;
    float4x4 model;
};

struct PushData {
    ShaderData* data;
    Vertex* vertices;
};

[[vk::push_constant]]
ConstantBuffer<PushData> push;

struct VSOutput {
    float4 pos   : SV_Position;
    float4 color : TEXCOORD0;
};

float4 unpackColor(uint color) {
    return float4(
        (float)(color & 0xFFu),
        (float)((color >> 8) & 0xFFu),
        (float)((color >> 16) & 0xFFu),
        (float)(color >> 24)
    ) / 255.0;
}

[shader("vertex")]
VSOutput vertex(uint id : SV_VulkanVertexID) {
    ShaderData* data = push.data;
    Vertex vertex = push.vertices[id];
    float4 world = mul(data -> model, float4(vertex.x, vertex.y, vertex.z, 1.0));

    VSOutput output;

    output.pos = mul(data -> projection, world);
    output.color = unpackColor(vertex.color);

    return output;
}

[shader("fragment")]
float4 fragment(VSOutput input) : SV_Target {
    return input.color;
}