attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_srcRect;
uniform vec4 u_dstRect;
uniform float u_useTexCoords;
uniform float u_useFishEye;
varying vec3 v_pos;

void main() {
    vec4 pos = u_worldTrans * a_position;
    v_pos = pos.xyz;
	gl_Position = u_projTrans * pos;
}