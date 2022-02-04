
#version 330 core
in vec4 fPos;
in vec4 fNormal;
in vec4 fColor;
in vec2 fTexCoord;
in vec2 fGridCoord;
in vec3 fId;
//in float fSelected;
//in float fAux;

layout(location = 0) out vec4 FragColor;
layout(location = 1) out vec4 BrightColor;
layout(location = 2) out vec4 FragId;

struct Light {
	vec4 position;
	vec4 direction;
	vec4 color;
	float innerCone;
	float outerCone;
};
uniform sampler2D texture0;
uniform sampler2D texture5;
uniform Light uSpotLights[4];
uniform vec4 uViewPos;
uniform float uTextureFactor = 1.0;
uniform float uColorFactor = 1.0;
uniform float uAmbientIntensity = 0.1;
uniform float time;

vec2 texSize = textureSize(texture0, 0);
vec4 selectColor = vec4(1.0,1.0,1.0,1.0);

const float specularStrength = 0.2;
const float specularExponent = 32;
const float attenuationC = 1.0, attenuationL = 0.14, attenuationQ = 0.10; // constant, linear and quadratic terms for light attenuation

vec4 normal = normalize(fNormal);
vec4 viewDir = normalize(uViewPos - fPos);


vec4 spotLight(Light light) {
	vec4 lightDir = normalize(light.position - fPos);
	float dist = distance(light.position, fPos);
	float area = attenuationC + (attenuationL * dist + attenuationQ * dist*dist);

	float theta = dot(lightDir, normalize(-light.direction));
	float epsilon = light.innerCone - light.outerCone;
	float spotIntensity = max((theta - light.outerCone) / epsilon, 0.0);

	float diff = max(dot(normal, lightDir), 0.0);
	vec4 diffuse = spotIntensity * diff * light.color;

	vec4 reflectDir = reflect(-lightDir, normal);

	float spec = pow(max(dot(viewDir, reflectDir), 0.0), specularExponent);
	vec4 specular = spotIntensity * specularStrength * spec * light.color;

	return (diffuse + specular)/area; // * texture(texture0, fTexCoord/texSize);
}

vec4 pointLight(Light light) {
	vec4 lightDir = normalize(light.position - fPos);
	float dist = distance(light.position, fPos);
	float area = attenuationC + (attenuationL * dist + attenuationQ * dist*dist)*2;

	float diff = max(dot(normal, lightDir), 0.0);
	vec4 diffuse = diff * light.color;

	vec4 reflectDir = reflect(-lightDir, normal);

	float spec = pow(max(dot(viewDir, reflectDir), 0.0), specularExponent);
	vec4 specular = specularStrength * spec * light.color;

	return (uAmbientIntensity + diffuse + specular)/area; // * texture(texture0, fTexCoord/texSize);
}

void main()
{
//	float sel = mix(fSelected, fSelected*time, 0.25);
//	int aux = int(fAux);
//	int lights[8];
//	for(int i = 0; i < 8; i++)
//		lights[i] = (aux>>i) & 1;

	vec4 activeLights = vec4(.1,.1,.1,.1);
	if(fGridCoord.x >= 0)
		activeLights = texture(texture5, fGridCoord);
	vec4 color = uColorFactor * fColor + uTextureFactor * texture(texture0, fTexCoord/texSize);
	FragColor  = .1*uAmbientIntensity * color;
	FragColor += spotLight(uSpotLights[0]) * color;
	FragColor += activeLights.r * pointLight(uSpotLights[1]) * color;
	FragColor += activeLights.g * pointLight(uSpotLights[2]) * color;
	FragColor += activeLights.b * pointLight(uSpotLights[3]) * color;
//	FragColor += activeLights.r * vec4(.5,0,0,1);
//	FragColor += activeLights.g * vec4(0,.5,0,1);
//	FragColor += activeLights.b * vec4(0,0,.5,1);
    float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    if(brightness > .8)
        BrightColor = vec4(FragColor.rgb, 1.0);
    else
        BrightColor = vec4(0.0, 0.0, 0.0, 1.0);

	FragId = vec4(fId/255.0, 1.0);
}
