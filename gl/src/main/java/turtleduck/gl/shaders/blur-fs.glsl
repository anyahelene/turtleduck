
#version 430 core

in vec2 fTexCoord;
out vec4 FragColor;
uniform sampler2D texture0;

uniform ivec2 uDirection = ivec2(1,0);
uniform float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
const int distance = 4;

void main()
{
	ivec2 pos = ivec2(fTexCoord*textureSize(texture0, 0));

	vec3 col = vec3(0);
	for(int x = -distance; x <= distance; x++) {
		col += texelFetch(texture0, pos+uDirection*x,0).rgb * weight[abs(x)];
	}
	FragColor = vec4(col, 1.0);
}

