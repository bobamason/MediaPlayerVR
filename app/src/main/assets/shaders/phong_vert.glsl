//attribute vec4 a_color;
attribute vec4 a_position;
attribute vec3 a_normal;
//attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform mat4 u_modelView;
//uniform float u_time;
//varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_position;
//varying vec3 v_normal;

void main() {
    v_position = vec3(u_modelView * a_position);
    v_normal = vec3(u_modelView * vec4(a_normal, 0.0));
	gl_Position = u_projTrans * u_worldTrans * a_position;
}