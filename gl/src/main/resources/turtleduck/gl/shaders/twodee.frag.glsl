#version 300 es

precision mediump float;

in TwoDeeFrag {
	vec4 fColor;
	vec2 fTexCoord;
	vec4 fNormal;
	flat int fTexNum;
};

layout (location = 0) out vec4 FragColor;
layout (location = 1) out vec4 BrightColor;

uniform sampler2D texture0;
uniform sampler2D texture1;
uniform highp float uPointScale;

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
	if(uPointScale > 0.0) {
		vec2 coord = 2.0*(gl_PointCoord - vec2(.5,.5));
		if((coord.x*coord.x + coord.y*coord.y) > 1.0)
			discard;
	} else if((FragColor.r+FragColor.g+FragColor.b+FragColor.a) < 0.0001)
	discard;
    float brightness = dot(FragColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    BrightColor = vec4(FragColor.rgb - FragColor.a, 0);
    BrightColor /= FragColor.a;
    if(max(FragColor.r,FragColor.g) > FragColor.a)
        BrightColor = vec4(FragColor.rgb, 1.0);
    else
        BrightColor = vec4(0.0, 0.0, 0.0, 1.0);
   // color = BrightColor;
   // BrightColor = FragColor;
   // FragColor = color * 1 ;//+ FragColor * 1;
   // FragColor = vec4(1.0,0.0,0.0,1.0);
}
