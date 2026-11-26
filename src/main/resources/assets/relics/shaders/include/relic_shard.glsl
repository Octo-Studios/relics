uniform float Cracks[10];

const int crackStage[5] = int[5](0, 2, 0, 3, 1);
const vec3 turns[5] = vec3[5](vec3(0.55, -0.98, -0.40), vec3(-0.92, 0.46, 0.66), vec3(0.86, 0.66, 0.43), vec3(-0.42, -1.16, -0.72), vec3(-1.04, -0.35, 0.35));
const float thickness = 0.125;

vec2 crackVector(int i) {
    return vec2(Cracks[2 * i], Cracks[2 * i + 1]);
}

bool insideShard(vec2 p, vec2 start, vec2 end) {
    return start.x * p.y >= start.y * p.x && end.x * p.y < end.y * p.x;
}

vec2 sectorBisector(vec2 start, vec2 end) {
    vec2 chord = normalize(start) - normalize(end);
    return normalize(vec2(-chord.y, chord.x));
}

mat3 rotation(vec3 angles) {
    vec3 s = sin(angles), c = cos(angles);
    return mat3(c.z, s.z, 0.0, -s.z, c.z, 0.0, 0.0, 0.0, 1.0)
         * mat3(c.y, 0.0, -s.y, 0.0, 1.0, 0.0, s.y, 0.0, c.y)
         * mat3(1.0, 0.0, 0.0, 0.0, c.x, s.x, 0.0, -s.x, c.x);
}

struct ShardPose {
    vec2 pivot;
    vec2 center;
    mat3 axes;
    float scale;
};

ShardPose shardPose(int id, vec2 outward, vec4 opened, float travel, float burst) {
    vec2 group = sectorBisector(crackVector(0), crackVector(2));
    if (id < 2)
        group = mix(group, outward, opened.z);
    else {
        group = mix(-group, id == 4 ? outward : sectorBisector(crackVector(2), crackVector(4)), opened.y);
        group = mix(group, outward, opened.w);
    }

    vec2 pivot = outward * (0.28 * travel);
    vec2 center = pivot + group * (0.065 * opened.x) + outward * travel * (0.66 + 0.06 * float(id));
    center.y += 0.24 * burst * burst;
    vec3 tilt = vec3(-group.y, group.x, 0.0) * (0.25 * opened.x);
    mat3 axes = rotation(mix(tilt, turns[id], burst));
    return ShardPose(pivot, center, axes, 1.0 + 0.18 * travel);
}
