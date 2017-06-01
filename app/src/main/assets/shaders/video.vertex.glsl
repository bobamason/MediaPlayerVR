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

//vec2 equirectangularProj(vec3 p){
//    float u = atan(p.x, -p.z) / PI;
//    float v = acos(p.y) / PI;
//    return vec2(u * 0.5 + 0.5, v);
////    return vec2(p.x * 0.5 + 0.5, 1.0 - (p.y * 0.5 + 0.5));
//}
//
//vec2 fishEyeProj(vec3 p){
//    float theta = atan(p.y, p.x);
//    float r = 2.0 * atan(length(p.xy), -p.z) / PI;
//    float u = r * cos(theta);
//    float v = r * sin(theta);
//    return vec2(u * 0.5 + 0.5, 1.0 - (v * 0.5 + 0.5));
//}

void main() {
    vec4 pos = u_worldTrans * a_position;
    vec2 scale = u_srcRect.zw / u_dstRect.zw;
    vec2 offset = u_srcRect.xy - u_dstRect.xy;
    vec2 tc;
//    if(u_useTexCoords > 0.5) {
        tc = a_texCoord0;
//    } else {
//        if(u_useFishEye > 0.5) {
//            tc = fishEyeProj(a_position.xyz);
//        } else {
//            tc = equirectangularProj(a_position.xyz);
//        }
//    }
    v_texCoord = scale * tc + offset;
	gl_Position = u_projTrans * pos;
}