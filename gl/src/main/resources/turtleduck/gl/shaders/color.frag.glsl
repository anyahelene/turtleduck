#version 300 es
precision mediump float;

in vec4 fColor;
in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;

out vec4 FragColor;

uniform sampler2D texture0;
uniform sampler2D texture1;
uniform vec4 uLightPos;
uniform vec4 uViewPos;


vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);
vec4 selectColor = vec4(1.0, 1.0, 1.0, 1.0);

const float specularStrength = 1.0;
const float shininess = 1.1;
const float ambient = 1.0;

struct Light {
    vec3  position;
    vec3  direction;
    float cutOff;
};


/*
 float near = 0.1;
 float far  = 100.0;

 float LinearizeDepth(float depth)
 {
 float z = depth * 2.0 - 1.0; // back to NDC
 return (2.0 * near * far) / (far + near - z * (far - near));
 //    float linearDepth = (2.0 * near*far) / (z * (far - near) - (far + near) );
 }
 */
void main() {
	//ivec2 texSize = textureSize(texture0, 0);

	bool shading = true;
	vec4 norm = normalize(fNormal);
	vec4 lightDir = normalize(uLightPos - fPos);
	float diff = max(dot(norm, lightDir), 0.0);
	vec4 diffuse = diff * lightColor + max(dot(norm, vec4(.5,0,1,0)),0.0) * vec4(1.0,0.7,.2,0);

	vec4 viewDir = normalize(uViewPos - fPos);
	vec4 reflectDir = reflect(-lightDir, norm);
	float foo = dot(norm, vec4(0,1,0,0));

	float spec = max(dot(viewDir, reflectDir), 0.0);
	vec4 specular = specularStrength * pow(spec, shininess) * lightColor;
	FragColor = (ambient + diffuse + specular) * fColor; // texture(texture0, fTexCoord/texSize); //vec4(fColor, 1.0);

	//	FragColor += mix(vec4(1,0,0,1), vec4(0,1,0,1), fTexCoord.x);
	if (false) {
		float x = 2.0 * abs(fTexCoord.x - .5);
		if (fTexCoord.x < 0.025 || fTexCoord.x > 0.975)
			FragColor += vec4(1, 1, 1, 1);
		else if (fTexCoord.x > 0.225 && fTexCoord.x < 0.275)
			FragColor += vec4(0, 1, 0, 1);
		else if (fTexCoord.x > 0.475 && fTexCoord.x < 0.525)
			FragColor += vec4(0, 0, 1, 1);
		else if (fTexCoord.x > 0.725 && fTexCoord.x < 0.775)
			FragColor += vec4(1, 0, 0, 1);
		FragColor /= 2.0;
	}

	FragColor = mix(FragColor, fColor, 0.1);
	if (!shading) {
		FragColor = fColor;
		// FragColor = fNormal;
	}

	//FragColor.a = spec >.8 ? 1.0 : fColor.a;
	//if(spec > 0.05)
//	FragColor.rgb = vec3(1,0,0);
	float near = 20.0;
	float far = 30.0;
	float depth = gl_FragCoord.z / far;
	float z = depth * 2.0 - 1.0; // back to NDC
	z = (2.0 * near * far) / (far + near - z * (far - near));
	//FragColor = vec4(vec3(z), 1.0);
	//   if(FragColor.a <= 0.1)
	//      discard;
}

/*#version 330

 #define PI 3.14159265359

 uniform sampler2D tex;
 varying vec3 dir;
 varying vec3 normal;

 //out vec4 fragColor;

 float rand(vec2 co){
 return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
 }

 void main(void) {
 }
 */
