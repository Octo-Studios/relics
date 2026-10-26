#version 330
#define PI 3.14
#define POINT_COUNT 256
uniform float revealRadiuses[POINT_COUNT/2]; // 0 -> 1
uniform float noiseSpreads[POINT_COUNT/2];
uniform float positions[POINT_COUNT];
uniform float greenRadius;
//uniform vec4 color;
uniform vec2 size;
uniform float time;
uniform float pixelCount;
uniform int pointCount;
uniform vec3 col1;
uniform vec3 col2;

uniform sampler2D Sampler0;

in vec2 texCoord0;

out vec4 fragColor;




float hash1(float p){
    p *= 434.0;
    p = fract(p * .1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

const uint k = 1103515245U;

vec3 hashwithoutsine33( uvec3 x )
{
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;
    x = ((x>>8U)^x.yzx)*k;

    return vec3(x)*(1.0/float(0xffffffffU));
}

vec3 generateGradientVector(float x,float y,float z){

    return normalize((hashwithoutsine33(uvec3(x*2329.,y*1209.,z*2239.)) -0.5) * 2.);

}

float dotPr(float dx, float dy, float dz,float lx,float ly,float lz,float xo,float yo,float zo){

    vec3 gradient = generateGradientVector(
    dx + xo,
    dy + yo,
    dz + zo
    );

    vec3 toLocal = vec3(
    lx - xo,
    ly - yo,
    lz - zo
    );

    return dot(toLocal,gradient);
}

float fade(float t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float perlinNoise3d(float x,float y,float z,float sections){

    x = x * sections;
    y = y * sections;
    z = z * sections;

    float dx = floor(x);
    float dy = floor(y);
    float dz = floor(z);

    float lx = fract(x);
    float ly = fract(y);
    float lz = fract(z);

    float val1 = dotPr(dx,dy,dz,lx,ly,lz,0.,0.,0.);
    float val2 = dotPr(dx,dy,dz,lx,ly,lz,1.,0.,0.);

    float val3 = dotPr(dx,dy,dz,lx,ly,lz,0.,1.,0.);
    float val4 = dotPr(dx,dy,dz,lx,ly,lz,1.,1.,0.);

    float val5 = dotPr(dx,dy,dz,lx,ly,lz,0.,0.,1.);
    float val6 = dotPr(dx,dy,dz,lx,ly,lz,1.,0.,1.);

    float val7 = dotPr(dx,dy,dz,lx,ly,lz,0.,1.,1.);
    float val8 = dotPr(dx,dy,dz,lx,ly,lz,1.,1.,1.);


    lx = fade(lx);
    ly = fade(ly);
    lz = fade(lz);

    float val9 = mix(val1,val2,lx);
    float val10 = mix(val3,val4,lx);
    float val11 = mix(val5,val6,lx);
    float val12 = mix(val7,val8,lx);

    float val13 = mix(val9,val10,ly);
    float val14 = mix(val11,val12,ly);

    float final = mix(val13,val14,lz);

    return final;
}



vec2 normalizeCoords(vec2 coord){
    float m = size.y / size.x;
    coord.y = coord.y * m;
    return coord;
}



void main(){

    vec2 ptc = floor(normalizeCoords(texCoord0) * pixelCount);
    float dist = 1000000;
    float revealRadius = 0;
    vec2 point;
    int pointid;
    float mod;
    float noisePixelSpread = 5;

    for (int i = 0; i < pointCount * 2; i += 2){
        float rad = revealRadiuses[i / 2];
        float nSpread = noiseSpreads[i / 2];

        vec2 v = floor(normalizeCoords(vec2(positions[i], positions[i + 1])) * pixelCount);
        vec2 b = ptc - v;

        float bLength = length(b);

        if ((bLength - nSpread * 1.75 - 0.5) / rad >= dist)
            continue;

        float additionalLength = 0;
        if (bLength > 0){
            vec2 bn = b / bLength;
            vec2 b1 = bn * rad;
            additionalLength = perlinNoise3d(b1.x + 0.143223,b1.y + 0.143223,time + 0.22343 * i,10) * nSpread;
            additionalLength = round(additionalLength);
        }

        float l = (bLength + additionalLength) / rad;
        if (l < dist){
            noisePixelSpread = nSpread;
            revealRadius = rad;
            dist = l;
            point = v;
            pointid = i;
            mod = additionalLength;
        }
    }


    vec2 pms = point;



    vec2 b = ptc - pms;

    float len = (length(b) + mod) / pixelCount;
    float v = 0;
    float coefficient = 0;
    if (len > revealRadius){
        v = 1;
        float dist = len - revealRadius;
        coefficient = max(0,greenRadius - dist) / greenRadius;
    }


    vec4 color = texture(Sampler0,texCoord0);
    vec4 col = color * v;

    mod = (mod / noisePixelSpread + 1) / 2;

    vec3 additiveCol = mix(col1,col2,mod);

    col.rgb += additiveCol * coefficient;


    fragColor = col;
}
