
#version 430 core

in vec2 fTexCoord;
out vec4 FragColor;
uniform sampler2D texture0;
uniform sampler2D texture1;
mat3 weights = mat3(.1,.1,.1, //
		            .1,.2,.1, //
					.1,.1,.1);
vec3 blur() {
	ivec2 pos = ivec2(fTexCoord * textureSize(texture1, 0));
	vec3 col = vec3(0,0,0);
	for(int y = 0; y < 3; y++)
		for(int x = 0; x < 3; x++)
			col += texelFetch(texture1, pos+ivec2(x-1,y-1), 0).rgb * weights[x][y];
	return col;
}
void main()
{
    const float gamma = 1.2;
    const float exposure = .7;
    vec3 color = texture(texture0, fTexCoord).rgb;
    vec3 bright = texture(texture1, fTexCoord).rgb;
    vec3 result = vec3(1.0) - exp(-(color+bright) * exposure); // tone mapping
    result = pow(result, vec3(1.0 / gamma)); // gamma
    FragColor = vec4(result, 1.0);
}
