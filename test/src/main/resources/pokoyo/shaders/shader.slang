struct Vertex {
    float x;
    float y;
    float z;
    uint color;
};

struct VertexArray {
    Vertex v[1048576];
};

struct ShaderData {
    float4 proj0;  float4 proj1;  float4 proj2;  float4 proj3;
    float4 model0; float4 model1; float4 model2; float4 model3;
};

struct PushData {
    ShaderData* data;
    VertexArray* vertices;
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

float4 unpackColor(uint c) {
    return float4(
        (float)(c & 0xFFu),
        (float)((c >> 8) & 0xFFu),
        (float)((c >> 16) & 0xFFu),
        (float)(c >> 24)
    ) / 255.0;
}

[shader("vertex")]
VSOutput vertex(uint id : SV_VulkanVertexID) {
    ShaderData* data = push.data;
    Vertex v = push.vertices->v[id];

    float2 p = float2((id & 1) * 2.0 - 1.0, (id >> 1) * 2.0 - 1.0) * 0.5;

    VSOutput output;
    output.pos = mulCols(data->proj0, data->proj1, data->proj2, data->proj3, float4(p, -2.0, 1.0));
    output.color = unpackColor(v.color);
    return output;
}

[shader("fragment")]
float4 fragment(VSOutput input) : SV_Target {
    return input.color;
}