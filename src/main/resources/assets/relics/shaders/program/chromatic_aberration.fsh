#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float Strength;
uniform int ColorCount;
uniform float Colors[24];

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 src = texture(DiffuseSampler, texCoord);
    vec3 base = src.rgb;

    int count = min(ColorCount, 8);
    if (count <= 0 || Strength <= 0.0) {
        fragColor = src;
        return;
    }

    vec2 pixelSize = 1.0 / ScreenSize;

    vec2 center = vec2(0.5, 0.5);
    vec2 dd = texCoord - center;
    float dist = length(dd);
    vec2 radial = dist > 0.0 ? dd / dist : vec2(1.0, 0.0);

    vec3 ghost = vec3(0.0);
    float usedCount = 0.0;

    for (int i = 0; i < count; i++) {
        int idx = i * 3;
        vec3 mask = vec3(Colors[idx], Colors[idx + 1], Colors[idx + 2]);
        if (mask.r <= 0.0 && mask.g <= 0.0 && mask.b <= 0.0) {
            continue;
        }

        float t = (float(i) + 0.5) / float(count);
        float radius = Strength * (8.0 + 16.0 * t);
        float dir = (i % 2 == 0) ? -1.0 : 1.0;
        vec2 offset = radial * pixelSize * radius * dir;

        vec3 s = texture(DiffuseSampler, texCoord + offset).rgb;
        vec3 tinted = s * mask * 2.5;

        ghost += tinted;
        usedCount += 1.0;
    }

    if (usedCount <= 0.0) {
        fragColor = src;
        return;
    }

    ghost /= usedCount;

    float radialFactor = smoothstep(0.0, 0.9, dist);
    float k = clamp(Strength, 0.0, 1.0) * radialFactor;

    vec3 color = mix(base, ghost, k);

    fragColor = vec4(clamp(color, 0.0, 1.0), src.a);
}