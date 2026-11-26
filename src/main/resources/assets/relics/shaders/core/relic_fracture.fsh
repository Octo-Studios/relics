#version 150

#moj_import <relics:relic_shard.glsl>

uniform sampler2D Sampler0;
uniform float Stress;
uniform float Impact;
uniform float Burst;

in vec2 texCoord0;
out vec4 fragColor;

vec4 sourcePixel(ivec2 cell, ivec2 size) {
    if (any(lessThan(cell, ivec2(0))) || any(greaterThanEqual(cell, size)))
        return vec4(0.0);
    return texelFetch(Sampler0, ivec2(cell.x, size.y - 1 - cell.y), 0);
}

float nextEdge(float fraction, float span) {
    if (span > 0.0) return (1.0 - fraction) / span;
    if (span < 0.0) return -fraction / span;
    return 2.0;
}

void drawPiece(int id, vec2 start, vec2 end, vec2 p, vec4 opened, float travel, inout float depth, inout vec4 shard) {
    ShardPose pose = shardPose(id, sectorBisector(start, end), opened, travel, Burst);
    mat3 inverseAxes = transpose(pose.axes);
    vec3 local = inverseAxes * vec3(p - pose.center, 0.0) / pose.scale;
    vec3 view = inverseAxes[2] / pose.scale;
    float front = -local.z / view.z;
    float rayLength = thickness / view.z;

    ivec2 size = textureSize(Sampler0, 0);
    vec2 pixels = vec2(size) * 0.5;
    vec2 grid = (pose.pivot + local.xy + front * view.xy + 1.0) * pixels;
    vec2 span = -rayLength * view.xy * pixels;
    if (any(lessThan(max(grid, grid + span), vec2(0.0))) || any(greaterThanEqual(min(grid, grid + span), vec2(size))))
        return;

    ivec2 cell = ivec2(floor(grid)), stepCell = ivec2(sign(span));
    ivec2 previousCell = cell;
    vec2 crossing = vec2(nextEdge(fract(grid.x), span.x), nextEdge(fract(grid.y), span.y));
    vec2 stride = vec2(nextEdge(0.0, abs(span.x)), nextEdge(0.0, abs(span.y)));
    vec3 normal = pose.axes[2];
    float t = 0.0;

    while (t <= 1.0 && (shard.a == 0.0 || front - t * rayLength > depth)) {
        if (insideShard((vec2(cell) + 0.5) / pixels - 1.0, start, end)) {
            vec4 texel = sourcePixel(cell, size);
            if (texel.a > 0.0) {
                float shade = t == 0.0 ? 0.78 + 0.22 * normal.z : 0.61 + 0.25 * dot(normal, vec3(-0.48, -0.64, 0.60));
                shard = vec4(texel.rgb * shade, texel.a);
                if (Burst > 0.0 && Burst < 1.0 && t > 0.0 && !insideShard((vec2(previousCell) + 0.5) / pixels - 1.0, start, end) && sourcePixel(previousCell, size).a > 0.0) {
                    float heat = smoothstep(0.0, 0.12, Burst) * (1.0 - smoothstep(0.35, 1.0, Burst));
                    shard.rgb = mix(shard.rgb, vec3(1.0, 0.84, 0.55), 0.80 * heat);
                }
                depth = front - t * rayLength;
                return;
            }
        }

        t = min(crossing.x, crossing.y);
        previousCell = cell;
        bvec2 crossed = lessThanEqual(crossing, vec2(t));
        if (crossed.x) {
            cell.x += stepCell.x;
            crossing.x += stride.x;
            normal = -float(stepCell.x) * pose.axes[0];
        }
        if (crossed.y) {
            cell.y += stepCell.y;
            crossing.y += stride.y;
            normal = -float(stepCell.y) * pose.axes[1];
        }
    }
}

float beam(vec2 p, vec2 direction, float reach, vec2 pixelSize) {
    float rayLength = length(direction);
    reach *= rayLength;
    vec2 axis = direction / rayLength;
    float along = dot(p, axis);
    if (along < 0.0 || along >= reach)
        return 0.0;

    float side = abs(p.x * axis.y - p.y * axis.x);
    float halfPixel = 0.5 * max(abs(axis.y) * pixelSize.x, abs(axis.x) * pixelSize.y);
    float taper = 1.0 - along / reach;
    if (side > halfPixel + 0.065 * taper * taper)
        return 0.0;
    return along < 0.70 * reach && side <= halfPixel + 0.025 * taper ? 1.0 : 0.5;
}

void main() {
    vec2 p = (texCoord0 - 0.5) * 4.0;
    p -= Impact * Impact * vec2(0.028 * sin(Impact * 15.0), 0.016 * cos(Impact * 12.0));
    float travel = Burst * (2.0 - Burst);
    vec4 opened = Burst > 0.0 ? vec4(1.0) : smoothstep(vec4(0.0), vec4(1.0), vec4(Stress) - vec4(0.0, 1.0, 2.0, 3.0));
    float depth = 0.0;
    vec4 shard = vec4(0.0);
    ivec2 size = textureSize(Sampler0, 0);
    vec2 pixelSize = 2.0 / vec2(size);
    ivec2 cell = ivec2(floor((p + 1.0) / pixelSize));
    vec2 pixel = (vec2(cell) + 0.5) * pixelSize - 1.0;
    float flash = Burst > 0.0 ? 1.0 - smoothstep(0.0, 0.30, Burst) : 0.0;
    float reach = mix(0.62 + 0.07 * Stress + 0.24 * Impact, 1.92, smoothstep(0.0, 0.19, Burst)) * (1.0 - smoothstep(0.26, 0.69, Burst));
    float light = 0.0;
    for (int i = 0; i < 5; ++i) {
        vec2 crack = crackVector(i);
        drawPiece(i, crack, crackVector((i + 1) % 5), p, opened, travel, depth, shard);
        light = max(light, beam(pixel, crack, reach * opened[crackStage[i]], pixelSize));
    }
    if (abs(pixel.x) + abs(pixel.y) < (0.10 + 0.20 * flash) * opened.x * reach)
        light = 1.0;

    shard.rgb = mix(shard.rgb, vec3(1.0, 0.98, 0.94), 0.12 * Impact + 0.50 * flash);
    float ink = light > 0.0 ? 1.0 : 0.0;
    vec3 rayColor = light == 1.0 ? vec3(1.0) : vec3(0.75, 0.73, 0.86);
    if (Burst > 0.0 && Burst < 0.5 && ink == 0.0) {
        float t = 2.0 * Burst;
        float radius = mix(0.2, 1.72, t * (2.0 - t));
        vec2 d = abs(pixel);
        float octagon = max(max(d.x, d.y), (d.x + d.y) * 0.7071);
        if (abs(octagon - radius) < 0.6 * max(pixelSize.x, pixelSize.y)) {
            ink = 0.85 * (1.0 - smoothstep(0.4, 1.0, t));
            rayColor = vec3(1.0, 0.83, 0.53);
        }
    }
    ink *= 1.0 - shard.a;
    float alpha = shard.a + ink;
    if (alpha < 0.003)
        discard;
    fragColor = vec4((shard.rgb * shard.a + rayColor * ink) / alpha, alpha * (1.0 - smoothstep(0.55, 1.0, Burst)));
}
