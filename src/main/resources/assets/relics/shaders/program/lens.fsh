#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform float mouseX;
uniform float mouseY;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 uv = texCoord;
    float aspect = ScreenSize.x / ScreenSize.y;

    float mx = mouseX / ScreenSize.x;
    float my = mouseY / ScreenSize.y;

    my = 1.0 - my;

    vec2  center = vec2(mx, my);

    float radius = 0.28;
    float iorG   = 1.20;
    float iorR   = iorG + 0.02;
    float iorB   = iorG - 0.02;
    float depth  = 0.25;
    float rimK   = 0.5;

    vec2  p = (uv - center) * vec2(aspect, 1.0);
    float r = length(p);

    vec3 base = texture(DiffuseSampler, uv).rgb;

    if (r >= radius) {
        float edge = smoothstep(radius, radius + 0.01, r);

        base *= mix(1.0, 0.96, edge);
        fragColor = vec4(base, 1.0);

        return;
    }

    float z = sqrt(max(radius*radius - r*r, 0.0));
    vec3 n = normalize(vec3(p.x, p.y, z));
    vec3 I = vec3(0.0, 0.0, -1.0);

    vec3 dirR = refract(I, n, 1.0 / iorR);
    vec3 dirG = refract(I, n, 1.0 / iorG);
    vec3 dirB = refract(I, n, 1.0 / iorB);

    float tR = (-depth - 0.0) / dirR.z;
    float tG = (-depth - 0.0) / dirG.z;
    float tB = (-depth - 0.0) / dirB.z;

    vec2 hitR = vec2(p.x, p.y) + vec2(dirR.x, dirR.y) * tR;
    vec2 hitG = vec2(p.x, p.y) + vec2(dirG.x, dirG.y) * tG;
    vec2 hitB = vec2(p.x, p.y) + vec2(dirB.x, dirB.y) * tB;

    vec2 uvR = center + hitR / vec2(aspect, 1.0);
    vec2 uvG = center + hitG / vec2(aspect, 1.0);
    vec2 uvB = center + hitB / vec2(aspect, 1.0);

    uvR = clamp(uvR, vec2(0.001), vec2(0.999));
    uvG = clamp(uvG, vec2(0.001), vec2(0.999));
    uvB = clamp(uvB, vec2(0.001), vec2(0.999));

    float rC = texture(DiffuseSampler, uvR).r;
    float gC = texture(DiffuseSampler, uvG).g;
    float bC = texture(DiffuseSampler, uvB).b;
    vec3 col = vec3(rC, gC, bC);

    float cosI = clamp(dot(-I, n), 0.0, 1.0);
    float fres = pow(1.0 - cosI, 5.0);
    float rim  = smoothstep(radius, radius - 0.006, r) * fres;

    col += rimK * rim;

    float thickness = 1.0 - z / radius;
    col *= mix(1.0, 0.95, smoothstep(0.0, 1.0, thickness));

    fragColor = vec4(col, 1.0);
}