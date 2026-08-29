struct VSInput {
    [[vk::location(0)]]
    float3 inPos : POSITION;
}

struct VSOutput {
    float4 pos : SV_POSITION;
}

[shader("vertex")]
VSOutput vertex(VSInput input) {
    VSOutput output;

    output.pos = float4(input.inPos, 1.0);

    return output;
}

struct FSOutput {
    [[vk::location(0)]]
    float4 outFragColor : SV_Target;
}

[shader("fragment")]
FSOutput fragment() {
    FSOutput output;

    output.outFragColor = float4(1.0, 0.0, 0.0, 1.0);

    return output;
}