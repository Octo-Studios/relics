#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float t = clamp((time - 20.0) / 20.0, 0.0, 1.0);

    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);
    vec2 dir = uv - center;
    float dist = length(dir);

    vec2 baseUv = uv;

    vec2 dirNorm = dist > 0.0 ? dir / dist : vec2(0.0);
    float aberrationAmount = (0.01 + dist * 0.05) * t;
    vec2 offset = dirNorm * aberrationAmount;

    vec4 colR = texture(DiffuseSampler, baseUv + offset);
    vec4 colG = texture(DiffuseSampler, baseUv);
    vec4 colB = texture(DiffuseSampler, baseUv - offset);

    vec3 aberrated = vec3(colR.r, colG.g, colB.b);

    float gray = dot(aberrated, vec3(0.3, 0.59, 0.11));

    vec3 cursedBase = vec3(gray * 1.6, gray * 0.05, gray * 0.05);

    vec3 original = texture(DiffuseSampler, baseUv).rgb;

    vec3 cursed = mix(original, cursedBase, t);

    float vignette = 1.0 - smoothstep(0.25, 0.9, dist);
    cursed = mix(original, cursed * (vignette * 0.75 + 0.25), t);

    fragColor = vec4(cursed, 1.0);
}