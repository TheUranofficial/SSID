struct Vertex {
    float x;
    float y;
    float z;
    uint color;
};

struct ShaderData {
    float4 proj0;  float4 proj1;  float4 proj2;  float4 proj3;
    float4 model0; float4 model1; float4 model2; float4 model3;
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

float4 mulCols(float4 c0, float4 c1, float4 c2, float4 c3, float4 v) {
    return c0 * v.x + c1 * v.y + c2 * v.z + c3 * v.w;
}

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
    float4 world = mulCols(data->model0, data->model1, data->model2, data->model3, float4(vertex.x, vertex.y, vertex.z, 1.0));

    VSOutput output;

    output.pos = mulCols(data->proj0, data->proj1, data->proj2, data->proj3, world);
    output.color = unpackColor(vertex.color);

    return output;
}

[shader("fragment")]
float4 fragment(VSOutput input) : SV_Target {
    return input.color;
}