#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;

uniform mat4 ViewProjMat;

uniform int PointCount;
uniform float PointsWorld[192];

in vec2 texCoord;
out vec4 fragColor;

vec3 getPoint(int i) {
    int o = i * 3;
    return vec3(PointsWorld[o], PointsWorld[o + 1], PointsWorld[o + 2]);
}

vec2 projectToUV(vec3 worldPos, out float ok) {
    vec4 clipPos = ViewProjMat * vec4(worldPos, 1.0);

    if (clipPos.w <= 0.00001) {
        ok = 0.0;
        return vec2(0.0);
    }

    vec3 ndc = clipPos.xyz / clipPos.w;

    if (ndc.z < -1.0 || ndc.z > 1.0) {
        ok = 0.0;
        return vec2(0.0);
    }

    if (ndc.x < -1.0 || ndc.x > 1.0 || ndc.y < -1.0 || ndc.y > 1.0) {
        ok = 0.0;
        return vec2(0.0);
    }

    ok = 1.0;
    return ndc.xy * 0.5 + 0.5;
}

void main() {
    vec4 src = texture(DiffuseSampler, texCoord);

    float RadiusPx = 220.0;
    float Strength = 0.55;

    int pc = min(PointCount, 64);
    if (pc <= 0) {
        fragColor = src;
        return;
    }

    vec2 p = texCoord * ScreenSize;
    vec2 displaced = p;

    for (int i = 0; i < pc; i++) {
        float ok = 0.0;
        vec2 centerUV = projectToUV(getPoint(i), ok);
        if (ok < 0.5) continue;

        vec2 c = centerUV * ScreenSize;

        vec2 v = displaced - c;
        float dist = length(v);

        if (dist >= RadiusPx || dist <= 0.0001) continue;

        float x = dist / RadiusPx;
        float falloff = 1.0 - x;
        falloff = falloff * falloff * (3.0 - 2.0 * falloff);

        float k = Strength * falloff;

        float scale = 1.0 + k;

        displaced = c + v * scale;
    }

    vec2 uv2 = displaced / ScreenSize;

    if (uv2.x < 0.0 || uv2.x > 1.0 || uv2.y < 0.0 || uv2.y > 1.0) {
        fragColor = src;
        return;
    }

    fragColor = texture(DiffuseSampler, uv2);
}