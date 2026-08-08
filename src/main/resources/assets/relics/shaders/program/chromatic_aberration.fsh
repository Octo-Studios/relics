#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float Strength;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float FringeBoost = 2.5;

    vec2 center = vec2(0.5, 0.5);
    vec2 dir = texCoord - center;
    float dist = length(dir);

    float aberr = Strength * (0.35 + pow(dist, 1.5));
    vec2 offset = dir * aberr;

    vec2 rUV = clamp(texCoord + offset, 0.0, 1.0);
    vec2 gUV = texCoord;
    vec2 bUV = clamp(texCoord - offset, 0.0, 1.0);

    vec4 base = texture(DiffuseSampler, texCoord);
    float r = texture(DiffuseSampler, rUV).r;
    float g = texture(DiffuseSampler, gUV).g;
    float b = texture(DiffuseSampler, bUV).b;

    vec4 aberrColor = vec4(r, g, b, base.a);
    vec4 fringe = aberrColor - base;
    fringe.rgb *= FringeBoost;

    float mask = smoothstep(0.0, 1.0, dist);
    vec4 result = base + fringe * mask;

    fragColor = vec4(clamp(result.rgb, 0.0, 1.0), 1.0);
}
