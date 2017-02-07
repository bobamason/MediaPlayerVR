attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec2 u_texScale;
uniform vec2 u_texOffset;

varying vec2 v_texCoord;

void main() {
    vec4 pos = u_worldTrans * a_position;
    v_texCoord = u_texScale * a_texCoord0 + u_texOffset;
	gl_Position = u_projTrans * pos;
}