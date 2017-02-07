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

const float ONE_THIRD = 1.0 / 3.0;
uniform sampler2D diffuseTexture;
uniform vec4 u_color;
uniform vec4 u_color2;
uniform float u_time;
uniform float u_intensity;
varying vec2 v_texCoord;
//varying vec3 v_color;

void main(){
//    float lum = sin(v_texCoord.x) * 12.0 + sin(v_texCoord.y) * 8.5;
//    lum = fract(lum + u_time);
//    lum *= lum;
    gl_FragColor.rgb = mix(u_color2.rgb, u_color.rgb, u_intensity);
//    gl_FragColor.rgb = mix(u_color2.rgb, u_color.rgb, u_intensity) * texture2D(diffuseTexture, v_texCoord).rgb;
    gl_FragColor.a = 1.0;
//    gl_FragColor.a = step(0.625, dot(texture2D(diffuseTexture, v_texCoord).rgb, vec3(ONE_THIRD)));
    if (gl_FragColor.a <= 0.5)                                                                                          	       
        discard;
}