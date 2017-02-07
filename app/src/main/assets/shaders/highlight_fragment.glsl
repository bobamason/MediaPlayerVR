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
#define PI 3.141593
#define PI2 6.283185
//uniform sampler2D diffuseTexture;
uniform vec4 u_color;
uniform float u_time;
uniform float u_intensity;
varying vec2 v_texCoord;
varying vec3 v_position;

// spherical (radius, azimuth, inclination)
vec3 cartesianToSpherical(vec3 c){
    vec3 s = vec3(0.0);
    float r = length(c);
    if(r == 0.0)
        return s;
    s.x = r;
    s.y = atan(c.y, c.x);
    s.z = acos(c.z / r);
}

void main(){
//    vec3 s = cartesianToSpherical(v_position);
//    float p = dot(texture2D(diffuseTexture, v_texCoord).rgb, vec3(1.0 / 3.0));
//    float p = dot(texture2D(diffuseTexture, vec2((s.y + PI / PI2), (s.z / PI))).rgb, vec3(1.0 / 3.0));
//    float lum = sin(PI2 * p + u_time);
//    float lum = sin(s.y * 8.0 + u_time);
//    lum += sin(s.z * 6.0 + u_time);
//    float step = smoothstep(0.0, 0.5, lum);
//    gl_FragColor.rgb = mix(u_color.rgb * 0.5, u_color.rgb, step);
    gl_FragColor.rgb = u_color.rgb;
    gl_FragColor.a = 1.0;
}