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
uniform sampler2D u_diffuseTexture;
varying vec2 v_texCoord;
uniform vec4 u_clip;
uniform float u_tint;
uniform float u_brightness;
uniform float u_contrast;
uniform float u_colorTemp;
const vec3 ONE_HALF = vec3(0.5);

void main(){
    if(v_texCoord.x < u_clip[0] || v_texCoord.x > u_clip[2] || v_texCoord.y < u_clip[1] || v_texCoord.y > u_clip[3]){
        discard;
    } else {
        gl_FragColor = texture2D(u_diffuseTexture, v_texCoord);
    }
}