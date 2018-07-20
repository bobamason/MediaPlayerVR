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
#define PI 3.141592654
uniform samplerExternalOES textureOES;
varying vec2 v_texCoord;
uniform vec4 u_clip;
uniform float u_tint;
uniform float u_brightness;
uniform float u_contrast;
uniform float u_colorTemp;
uniform float u_alpha;
uniform int u_useFishEye;
uniform vec4 u_srcRect;
uniform vec4 u_dstRect;
const vec3 ONE_HALF = vec3(0.5);

vec2 convertFishEyeToSphere(vec2 coord, vec2 center, float fov, float radius){
    // Polar angles
    float theta = 2.0 * PI * (coord.x - center.x); // -pi to pi
    float phi = PI * (coord.y - center.y);	// -pi/2 to pi/2
                           
    // Vector in 3D space
    vec3 psph = vec3(0.0);
    psph.x = cos(phi) * sin(theta);
    psph.y = cos(phi) * cos(theta);
    psph.z = sin(phi);
                           	
    // Calculate fisheye angle and radius
    theta = atan(psph.z, psph.x);
    phi = atan(sqrt(psph.x * psph.x + psph.z * psph.z), psph.y);
    float r = phi / fov; 
    
    return vec2(0.5 + r * cos(theta), 0.5 + r * sin(theta));
}

void main(){
    vec2 tc;
    if(u_useFishEye == 1){
         tc = convertFishEyeToSphere(v_texCoord, vec2(0.5, 0.5), PI, 0.5);
    } else {
        tc = v_texCoord;
    }
        
    vec2 scale = u_srcRect.zw / u_dstRect.zw;
    vec2 offset = u_srcRect.xy - u_dstRect.xy;
    tc = scale * tc + offset;
    
    if(tc.x < u_clip[0] || tc.x > u_clip[2] || tc.y < u_clip[1] || tc.y > u_clip[3]){
        discard;
    }
        
    vec3 rgb = texture2D(textureOES, tc).rgb;
    rgb.r += u_colorTemp;
    rgb.g += u_tint;
    rgb.b -= u_colorTemp;
    rgb = (rgb - ONE_HALF) * max(u_contrast, 0.0) + ONE_HALF;
    rgb += vec3(u_brightness);
    gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), u_alpha);
}