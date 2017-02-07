attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
varying vec2 v_texCoord;
varying vec3 v_position;

void main() {
    vec4 pos = u_worldTrans * a_position;
    v_position = pos.xyz;
    v_texCoord = a_texCoord0;
	gl_Position = u_projTrans * pos;
}