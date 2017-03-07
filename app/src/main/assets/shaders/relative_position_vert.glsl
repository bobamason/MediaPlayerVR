//attribute vec4 a_color;
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec3 a_binormal;
//attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform mat4 u_modelView;
uniform float u_scale;
//uniform float u_time;
//varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_position;
//varying vec3 v_normal;

void main() {
    vec4 pos = a_position + vec4(a_binormal * u_scale, 0.0);
    v_position = vec3(u_modelView * pos);
    v_normal = vec3(u_modelView * vec4(a_normal, 0.0));
	gl_Position = u_projTrans * u_worldTrans * pos;
}