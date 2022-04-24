#version 330

in vec4 fColor;
//in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;
flat in float fPointSize;
flat in int fTexNum;

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BrightColor;

uniform sampler2D texture0;
uniform sampler2D texture1;

void main() {
	vec4 BrightColor;
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
	//	float gamma = 1/2.2;
	//	FragColor = pow(color,vec4(gamma));
	FragColor = color;
	if(fPointSize > 0) {
		vec2 coord = 2*(gl_PointCoord - vec2(.5,.5));
		float l = length(coord);
		l *= l;
		l = (coord.x*coord.x + coord.y*coord.y);
	//	if(l < .4)
	//		l = 0;
	//	color *=l;
		float area = 4*fPointSize;
		area = max(1, area);
		//FragColor.a *= fPointSize;
		if(l > fPointSize)
			discard;
	} else if (FragColor.a <= 0.001)
		discard;
    float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    BrightColor = vec4(FragColor.rgb - FragColor.a, 0);
    BrightColor /= FragColor.a;
    //if(max(FragColor.r,FragColor.g) > FragColor.a)
    //    BrightColor = vec4(FragColor.rgb, 1.0);
    //else
    //    BrightColor = vec4(0.0, 0.0, 0.0, 1.0);
    color = BrightColor;
    BrightColor = FragColor;
    FragColor = color * .0 + FragColor * 1;
}
