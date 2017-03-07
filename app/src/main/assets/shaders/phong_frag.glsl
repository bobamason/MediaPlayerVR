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
//uniform sampler2D diffuseTexture;
uniform vec4 u_color;
//uniform vec4 u_color2;
//uniform float u_time;
uniform float u_lightIntensity;
uniform vec3 u_lightPos;
uniform vec3 u_camPos;
//varying vec2 v_texCoord;
varying vec3 v_normal;
varying vec3 v_position;

void main(){
    float distance = length(u_lightPos - v_position);
    vec3 lightVector = normalize(u_lightPos - v_position);
    float diffuse = max(dot(v_normal, lightVector), 0.1) * u_lightIntensity;
    diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
    vec3 L = normalize(u_camPos - v_position);
    vec3 E = normalize(-v_position);
    vec3 R = normalize(-reflect(L, v_normal));
    float halfDotView = max(0.0, dot(R, E));
    float specular = clamp(pow(halfDotView, 20.0), 0.0, 1.0);
    gl_FragColor.rgb = u_color.rgb * diffuse + vec3(1.0) * specular;
    gl_FragColor.a = 1.0;
}