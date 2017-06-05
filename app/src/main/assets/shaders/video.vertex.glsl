attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_srcRect;
uniform vec4 u_dstRect;
uniform float u_useTexCoords;
uniform float u_useFishEye;
varying vec2 v_texCoord;
#define PI 3.14159265

vec3 equirectangularProj(vec2 p){
    vec3 pos = vec3(0.0);
    float azimuth = -p.x * PI * 2.0 - PI * 0.5;
    float elevation = p.y * PI;
    pos.x = cos(azimuth) * sin(elevation);
    pos.y = cos(elevation);
    pos.z = -sin(azimuth) * sin(elevation);
    return pos;
//    float u = atan(p.x, -p.z) / PI;
//    float v = acos(p.y) / PI;
//    return vec3(u * 0.5 + 0.5, v);
}

vec3 fishEyeProj(vec2 p){
    p -= vec2(0.5);
    p *= 2.0;
    float r = length(p);
    float azimuth = -p.x * PI * 2.0 - PI * 0.5;
    float elevation = p.y * PI;
    vec3 pos = vec3(0.0);
    pos.x = cos(azimuth) * sin(elevation);
    pos.y = cos(elevation);
    pos.z = -sin(azimuth) * sin(elevation);
    return pos;
//    float theta = atan(p.y, p.x);
//    float r = 2.0 * atan(length(p.xy), -p.z) / PI;
//    float u = r * cos(theta);
//    float v = r * sin(theta);
//    return vec2(u * 0.5 + 0.5, 1.0 - (v * 0.5 + 0.5));
}

void main() {
    vec4 pos = a_position;
    vec2 scale = u_srcRect.zw / u_dstRect.zw;
    vec2 offset = u_srcRect.xy - u_dstRect.xy;
    vec2 tc = a_texCoord0;
    if(u_useTexCoords < 0.5) {
        if(u_useFishEye > 0.5) {
            pos.xyz = fishEyeProj(tc);
        } else {
            pos.xyz = equirectangularProj(tc);
        }
    }
    v_texCoord = scale * tc + offset;
	gl_Position = u_projTrans * u_worldTrans * pos;
}