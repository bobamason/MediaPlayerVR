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
uniform sampler2D diffuseTexture;
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
    s.y = atan(-c.z, c.x);
    s.z = acos(c.y / r);
    return s;
}

void main(){
    vec3 s = cartesianToSpherical(v_position);
//    vec3 c = texture2D(diffuseTexture, vec2((s.y + PI) / PI2 * 4.0 + cos(u_time), s.z / PI * 4.0 + sin(u_time * 3.0))).rgb;
    gl_FragColor.rgb = ((sin(s.y * 16.0 + u_time) + sin(s.z * 4.0 + u_time) + cos(length(s.yz) + u_time)) * 0.5 + 0.5) * u_color.rgb;
    gl_FragColor.a = 1.0;
}