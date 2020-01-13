
#version 430 core

in vec4 fColor;
in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;

out vec4 FragColor;

uniform sampler2D texture0;
uniform sampler2D texture1;
uniform vec4 uLightPos;
uniform vec4 uViewPos;

vec2 texSize = textureSize(texture0, 0);
vec4 lightColor = vec4(1.0,1.0,1.0,1.0);
vec4 selectColor = vec4(1.0,1.0,1.0,1.0);

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
void main()
{
	vec4 norm = normalize(fNormal);
	vec4 lightDir = normalize(uLightPos - fPos);
	float diff = 1.2 * max(dot(norm, lightDir), 0.0);
	vec4 diffuse = diff * lightColor;

	vec4 viewDir = normalize(uViewPos - fPos);
	vec4 reflectDir = reflect(-lightDir, norm);

	float spec = pow(max(dot(viewDir, reflectDir), 0.0), specularExponent);
	vec4 specular = specularStrength * spec * lightColor;

    FragColor = (ambient + diffuse + specular) * fColor; // texture(texture0, fTexCoord/texSize); //vec4(fColor, 1.0);
    FragColor.a = fColor.a;
    if(FragColor.a <= 0.1)
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
