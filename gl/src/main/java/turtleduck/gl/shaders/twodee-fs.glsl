#version 430 core

in vec4 fColor;
in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;
in flat int fTexNum;

out vec4 FragColor;

uniform sampler2D texture0;
uniform sampler2D texture1;
uniform vec4 uLightPos;
uniform vec4 uViewPos;

vec2 texSize = textureSize(texture0, 0);
vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);
vec4 selectColor = vec4(1.0, 1.0, 1.0, 1.0);

const float specularStrength = 0.2;
const float specularExponent = 256;
const float ambient = 0.35;

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
	vec4 texCol1 = texture(texture0, fTexCoord);
	vec4 texCol2 = texture(texture1, fTexCoord);
	vec4 color;
	if (fTexNum == 1) {
		color = texCol1;
	} else if (fTexNum == 2) {
		color = texCol2;
	} else {
		color = fColor;
	}
//		float gamma = 1/2.2;
//		FragColor = pow(color,gamma.xxxx);
	FragColor = color;
	if (FragColor.a <= 0.01)
		discard;
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
