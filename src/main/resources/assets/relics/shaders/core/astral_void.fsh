#version 150

uniform sampler2D Sampler0;

uniform float Coverage;
uniform float Time;
uniform float Aspect;

in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);

    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    f = f * f * (3.0 - 2.0 * f);

    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), f.x), mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;

    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p = p * 2.02 + vec2(3.1, 1.7);
        amplitude *= 0.5;
    }

    return value;
}

void main() {
    if (Coverage <= 0.001) {
        discard;
    }

    vec2 uv = texCoord0 * 2.0 - 1.0;
    vec2 q = vec2(uv.x * Aspect, uv.y);

    float radius = length(q) / length(vec2(Aspect, 1.0));

    vec2 warp = vec2(fbm(q * 1.3 + vec2(Time * 0.15, 0.0)), fbm(q * 1.3 + vec2(5.2, Time * 0.12)));
    float n = fbm(q * 1.8 + warp * 1.5 + vec2(0.0, -Time * 0.1));

    float threshold = (1.0 - radius) * 0.55 + n * 0.45 + 0.1 + uv.y * 0.08;
    float coverage = mix(0.05, 0.86, Coverage);
    float x = coverage * 1.35 - threshold;

    float cover = smoothstep(0.0, 0.05, x);
    float rim = smoothstep(0.0, 0.015, x) * (1.0 - smoothstep(0.015, 0.09, x));
    float edge = 1.0 - smoothstep(0.0, 0.35, x);

    vec2 sampleUv = clamp(texCoord0 + (warp - 0.5) * 0.06 * edge, 0.001, 0.999);
    vec3 scene = texture(Sampler0, sampleUv).rgb;

    vec3 rimColor = mix(vec3(0.55, 0.35, 1.0), vec3(0.3, 0.8, 1.0), warp.x);
    vec3 color = scene + rimColor * rim * 0.8;

    fragColor = vec4(min(color, vec3(1.0)), max(cover, rim));
}
