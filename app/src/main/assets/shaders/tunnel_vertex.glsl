attribute vec4 a_color;
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform float u_intensity;
//uniform float u_time;
varying vec2 v_texCoord;
//varying vec3 v_color;

void main() {
//    mat4 scale = mat4(1.0);
//    scale[2][2] = 0.75 + u_intensity * 0.5;
//    vec4 pos = u_worldTrans * scale * a_position;
    vec4 pos = u_worldTrans * a_position;
//    v_texCoord = vec2(pos.z, cos(pos.x) + sin(pos.y));
//    float c = sin(length(pos.xy) * 0.75 + u_time) + 1.0 / 2.0;
//    float r = 1.0 - c;
//    float g = c;
//    float b = cos(pos.z * 0.05) + 1.0 / 2.0;
//    v_color = vec3(r, g, b);
//    pos.z += (a_texCoord0.x * 2.0 - 1.0) * -2.0 * u_intensity;
//    pos.z += 5.0 * u_intensity;
    pos.xyz += u_intensity * 8.0 * (a_color.rgb * 2.0 - vec3(1.0, 1.0, 1.0));
    v_texCoord = a_texCoord0;
	gl_Position = u_projTrans * pos;
}