#version 150

uniform float Time;
uniform vec3 Offset;

in vec3 direction;

out vec4 fragColor;

float hash(vec3 p) {
    p = fract(p * 0.3183099 + 0.1);
    p *= 17.0;

    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float noise(vec3 x) {
    vec3 i = floor(x);
    vec3 f = fract(x);

    f = f * f * (3.0 - 2.0 * f);

    float a = mix(hash(i + vec3(0.0, 0.0, 0.0)), hash(i + vec3(1.0, 0.0, 0.0)), f.x);
    float b = mix(hash(i + vec3(0.0, 1.0, 0.0)), hash(i + vec3(1.0, 1.0, 0.0)), f.x);
    float c = mix(hash(i + vec3(0.0, 0.0, 1.0)), hash(i + vec3(1.0, 0.0, 1.0)), f.x);
    float d = mix(hash(i + vec3(0.0, 1.0, 1.0)), hash(i + vec3(1.0, 1.0, 1.0)), f.x);

    return mix(mix(a, b, f.y), mix(c, d, f.y), f.z);
}

float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;

    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec3(1.7, 9.2, 3.1);
        amplitude *= 0.5;
    }

    return value;
}

void main() {
    vec3 d = normalize(direction);
    vec3 p = d * 2.2 + Offset * 0.015;
    vec3 drift = vec3(Time * 0.012, Time * 0.008, -Time * 0.01);

    vec3 warp = vec3(
        fbm(p + drift),
        fbm(p + vec3(5.2, 1.3, 8.1) - drift),
        fbm(p + vec3(2.7, 6.4, 1.9))
    );

    float n = fbm(p + warp * 1.6 + drift);

    float t = d.y * 2.2 + 0.25 * sin(d.x * 3.0 + Time * 0.05);
    float band = exp(-t * t);
    float density = smoothstep(0.35, 0.95, n) * (0.35 + 0.9 * band);

    vec3 colorA = vec3(0.30, 0.10, 0.75);
    vec3 colorB = vec3(0.10, 0.35, 0.95);
    vec3 colorC = vec3(0.95, 0.25, 0.65);

    vec3 color = mix(colorA, colorB, warp.x);
    color = mix(color, colorC, smoothstep(0.5, 0.9, warp.y) * 0.8);

    vec3 result = vec3(0.012, 0.006, 0.04) + color * density * 0.9;

    result += vec3(0.9, 0.7, 1.0) * density * density * density * 0.35;

    float dust = noise(d * 180.0);

    result += vec3(0.8, 0.85, 1.0) * smoothstep(0.93, 1.0, dust) * 0.5 * (0.6 + 0.4 * sin(Time * 2.0 + dust * 60.0));

    fragColor = vec4(result, 1.0);
}
