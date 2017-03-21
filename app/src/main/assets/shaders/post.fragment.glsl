#ifdef GL_ES 
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

uniform sampler2D u_texture;
uniform float u_time;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    vec2 tc = v_texCoord - vec2(0.5, 0.5);
    const float radius = 0.5;
    float angle = 0.0125 * sin(u_time);
//    float angle = 0.125;
    float dist = length(tc);
    float percent = (radius - dist) / radius;
    float theta = percent * percent * angle * 8.0;
    if(dist < radius){
        float s = sin(theta);
        float c = cos(theta);
        tc = vec2(dot(tc, vec2(c, -s)), dot(tc, vec2(s, c)));
    }
    tc += vec2(0.5, 0.5);
    gl_FragColor = texture2D(u_texture, tc) * v_color;
}