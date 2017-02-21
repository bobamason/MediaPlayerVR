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
uniform vec4 u_clip;

void main(){
    if(v_texCoord.x < u_clip[0] || v_texCoord.x > u_clip[2] || v_texCoord.y < u_clip[1] || v_texCoord.y > u_clip[3]){
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        gl_FragColor = texture2D(textureOES, v_texCoord);
    }
}