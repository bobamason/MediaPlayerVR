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
#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES textureOES;
varying vec2 v_texCoord;

void main(){
    if(v_texCoord.x < 0.0 || v_texCoord.x > 1.0 || v_texCoord.y < 0.0 || v_texCoord.y > 1.0){
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        vec2 coord2 = vec2(mod(v_texCoord.u + 0.5, 1.0), v_texCoord.v);
        gl_FragColor.rgb = texture2D(textureOES, v_texCoord).rgb - texture2D(textureOES, coord2).rgb;
        gl_FragColor.a = 1.0;
    }
}