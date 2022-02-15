#version 430 core

in vec4 fColor;
in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;
flat in int fTexNum;

out vec4 FragColor;

uniform sampler2D texture0;
uniform sampler2D texture1;

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
	FragColor = color; //vec4(gl_FragCoord.zzz, 1);
	if (FragColor.a <= 0.01)
		discard;
}
