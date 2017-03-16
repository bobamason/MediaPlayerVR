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
uniform float u_tint;
uniform float u_brightness;
uniform float u_contrast;
uniform float u_saturation;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main(){
    if(v_texCoord.x < u_clip[0] || v_texCoord.x > u_clip[2] || v_texCoord.y < u_clip[1] || v_texCoord.y > u_clip[3]){
        discard;
    } else {
        vec3 hsv = rgb2hsv(texture2D(textureOES, v_texCoord).rgb);
        hsv.x = clamp(u_tint + hsv.x, 0.0, 360.0);
        hsv.y = clamp(u_saturation * hsv.y, 0.0, 1.0);
        hsv.z = clamp(u_contrast * hsv.z, 0.0, 1.0);
        hsv.z = clamp(u_brightness + hsv.z, 0.0, 1.0);
        vec3 rgb = hsv2rgb(hsv);
        gl_FragColor = vec4(rgb, 1.0);
    }
}