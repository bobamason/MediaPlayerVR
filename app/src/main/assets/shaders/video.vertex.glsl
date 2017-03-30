attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;
uniform mat4 u_worldTrans;
uniform vec4 u_srcRect;
uniform vec4 u_dstRect;
varying vec2 v_texCoord;

void main() {
    vec4 pos = u_worldTrans * a_position;
    vec2 scale = u_srcRect.zw / u_dstRect.zw;
    vec2 offset = u_srcRect.xy - u_dstRect.xy;
    v_texCoord = scale * a_texCoord0 + offset;
	gl_Position = u_projTrans * pos;
}