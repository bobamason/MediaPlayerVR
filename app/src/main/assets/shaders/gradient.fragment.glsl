#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

varying vec3 v_pos;
uniform vec4 u_color1;
uniform vec4 u_color2;
uniform float u_gradient;

void main(){
    float t = clamp((v_pos.y + u_gradient) / (2.0 * u_gradient), 0.0, 1.0);
    gl_FragColor = mix(u_color1, u_color2, t);
}