struct Vertex {
    float3 position;
    float4 color;
}

struct VSOutput {
    float4 pos : SV_POSITION;
    float4 color : TEXCOORD0;
};

struct ShaderData {
    float4x4 projectionMatrix;
    float4x4 modelMatrix;
    Vertex* vertices;
};

[[vk::push_constant]]
ShaderData* shaderData;

[shader("vertex")]
VSOutput vertex(uint id: SV_VertexID) {
    VSOutput output;
    Vertex vertex = shaderData -> vertices[id];

    float4x4 mvp = mul(
        shaderData -> projectionMatrix,
        shaderData -> modelMatrix
    );

    output.pos = mul(
        mvp,
        float4(vertex.position, 1.0)
    );

    output.color = vertex.color;

    return output;
}

[shader("fragment")]
float4 fragment(VSOutput input) : SV_Target {
    float factor = input.pos.y * 0.005 + input.pos.x * 0.002;

    float3 a = float3(0.5, 0.5, 0.5);
    float3 b = float3(0.5, 0.5, 0.5);
    float3 c = float3(1.0, 1.0, 1.0);
    float3 d = float3(0.0, 0.33, 0.67);

    float3 chromaticColor = a + b * cos(6.28318 * (c * factor + d));

    chromaticColor = frac(chromaticColor * 1.5);

    return float4(chromaticColor, 1.0);
}