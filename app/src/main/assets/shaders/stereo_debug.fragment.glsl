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

uniform sampler2D u_texture;
varying vec2 v_texCoord;

void main(){
    vec4 c0 = texture2D(u_texture, v_texCoord);
    float x = v_texCoord.x;
    if(x > 0.5)
        c = c * vec4(1.0, 1.0, 0.1, 1.0);
    else
        c = c * vec4(0.1, 1.0, 1.0, 1.0);
    gl_FragColor = c;
}