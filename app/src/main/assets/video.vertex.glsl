attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_srcRect;
uniform vec4 u_dstRect;
varying vec2 v_texCoord;

void main() {
    vec4 pos = u_worldTrans * a_position;
    v_texCoord = (u_srcRect.zw * a_texCoord0 + u_srcRect.xy) / u_dstRect.zw - u_dstRect.xy;
	gl_Position = u_projTrans * pos;
}