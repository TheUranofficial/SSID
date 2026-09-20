struct VSInput {
    [[vk::location(0)]]
    float3 position : POSITION;
    [[vk::location(1)]]
    float4 color : COLOR;
};

struct VSOutput {
    float4 pos : SV_POSITION;
    float4 color : TEXCOORD0;
};

struct Matrices {
    float4x4 projectionMatrix;
    float4x4 modelMatrix;
};

[[vk::push_constant]]
Matrices pushConstants;

[shader("vertex")]
VSOutput vertex(VSInput input) {
    VSOutput output;

    output.pos = mul(pushConstants.projectionMatrix * pushConstants.modelMatrix, float4(input.position, 1.0));
    output.color = input.color;

    return output;
}

struct FSOutput {
    float4 color : SV_Target;
};

[shader("fragment")]
FSOutput fragment(VSOutput out) {
    FSOutput output;

    output.color = out.color;

    return output;
}