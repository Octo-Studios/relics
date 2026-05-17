#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform vec2 ScreenSize;
uniform float time;

in vec2 texCoord;
out vec4 fragColor;

float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

float hash(vec2 p) {
    return hash(dot(p, vec2(127.1, 311.7)));
}

float pulse(float speed, float offset) {
    return step(0.78, hash(floor(time * speed) + offset));
}

vec3 glitchColor(vec2 uv, float mask) {
    float t = time * 0.05;
    vec2 center = uv - vec2(0.5);
    float dist = length(center);

    float line = floor(uv.y * ScreenSize.y);
    float block = floor(uv.y * 42.0);
    vec2 cell = floor(uv * vec2(34.0, 19.0));
    float fineNoise = hash(vec2(line, floor(time * 2.0)));
    float blockNoise = hash(vec2(block, floor(time * 0.75)));
    float cellNoise = hash(cell + floor(time * 1.6));

    float burst = max(pulse(7.0, 1.0), pulse(11.0, 17.0));
    float heavyBurst = pulse(3.0, 41.0);
    float tearMask = step(0.86, blockNoise) * burst * mask;
    float jitterMask = step(0.985, fineNoise) * mask;
    float cellMask = step(0.965, cellNoise) * (0.45 + burst) * mask;
    float scanMask = step(0.5, fract((uv.y * ScreenSize.y + time * 1.8) * 0.5));

    float tearOffset = (hash(block + floor(time * 3.0)) - 0.5) * 0.085 * tearMask;
    float jitterOffset = (fineNoise - 0.5) * 0.018 * jitterMask;
    float waveOffset = sin((uv.y * 90.0) + t * 28.0) * 0.0025 * burst * mask;
    float roll = heavyBurst * sin(time * 0.17) * 0.018 * mask;
    vec2 shiftedUv = clamp(uv + vec2(tearOffset + jitterOffset + waveOffset, roll), vec2(0.001), vec2(0.999));

    vec2 blockUv = (cell + vec2(0.5)) / vec2(34.0, 19.0);
    blockUv.x += (hash(cell.y + floor(time * 4.0)) - 0.5) * 0.12 * cellMask;
    shiftedUv = mix(shiftedUv, clamp(blockUv, vec2(0.001), vec2(0.999)), cellMask * 0.7);

    float split = (0.0025 + 0.006 * burst + abs(tearOffset) * 0.28) * (0.65 + hash(block) * 0.7) * mask;
    vec2 redUv = clamp(shiftedUv + vec2(split, 0.0), vec2(0.001), vec2(0.999));
    vec2 blueUv = clamp(shiftedUv - vec2(split, 0.0), vec2(0.001), vec2(0.999));

    vec3 color;
    color.r = texture(DiffuseSampler, redUv).r;
    color.g = texture(DiffuseSampler, shiftedUv).g;
    color.b = texture(DiffuseSampler, blueUv).b;

    float staticNoise = hash(floor(uv * ScreenSize.xy * vec2(0.45, 0.22)) + floor(time * 8.0));
    color += (staticNoise - 0.5) * (0.035 + 0.06 * tearMask) * mask;

    float scanline = mix(0.95, 1.03, scanMask);
    color *= mix(1.0, scanline, mask);

    float bandFlash = tearMask * smoothstep(0.15, 0.95, hash(vec2(block, floor(time * 9.0))));
    color = mix(color, vec3(color.r * 1.25, color.g * 0.82, color.b * 1.45), bandFlash * 0.35);

    float invertMask = heavyBurst * step(0.992, hash(vec2(block, floor(time * 16.0)))) * mask;
    color = mix(color, 1.0 - color, invertMask * 0.22);

    float edgeBleed = smoothstep(0.42, 0.74, dist) * (0.2 + 0.35 * burst) * mask;
    color.rb += vec2(0.055, 0.085) * edgeBleed;
    color *= 1.0 - smoothstep(0.55, 0.88, dist) * 0.18 * mask;

    return clamp(color, 0.0, 1.0);
}

void main() {
    vec4 src = texture(DiffuseSampler, texCoord);
    float mask = texture(MaskSampler, texCoord).a;

    if (mask <= 0.01) {
        fragColor = vec4(src.rgb, 1.0);
        return;
    }

    fragColor = vec4(mix(src.rgb, glitchColor(texCoord, mask), mask), 1.0);
}
