attribute vec4 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projTrans;

varying vec2 v_rgbNW;
varying vec2 v_rgbNE;
varying vec2 v_rgbSW;
varying vec2 v_rgbSE;
varying vec2 v_rgbM;
varying vec2 v_tc;

uniform vec2 resolution;

void main() {
	gl_Position = u_projTrans * a_position;
	
	v_tc = a_texCoord0;
    vec2 fragCoord = v_tc * resolution;
    vec2 inverseVP = vec2(1.0 / resolution.x, 1.0 / resolution.y);
    v_rgbNW = (fragCoord + vec2(-1.0, -1.0)) * inverseVP;
    v_rgbNE = (fragCoord + vec2(1.0, -1.0)) * inverseVP;
    v_rgbSW = (fragCoord + vec2(-1.0, 1.0)) * inverseVP;
    v_rgbSE = (fragCoord + vec2(1.0, 1.0)) * inverseVP;
    v_rgbM = vec2(fragCoord * inverseVP);
}