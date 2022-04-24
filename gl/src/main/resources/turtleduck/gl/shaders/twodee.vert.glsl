
#version 410
layout (location = 0) in vec4 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

uniform Matrices {
	mat4 uModel;
	mat4 uNormal;
	mat4 uProjView;
};

//out vec4 fPos;

out TwoDeeFrag {
	vec4 fColor;
	vec2 fTexCoord;
	vec4 fNormal;
	flat float fPointSize;
	flat int fTexNum;
};

void main()
{
	//float g = (aPos.y + .5)/1.5;
	//float r = -aPos.x/0.866;
	//float b = 1.0 - r;
	
    fTexNum =  int(aPos.w);
  //  fPos = uModel * vec4(aPos.xy,-aPos.z/10, 1.0);
    gl_Position =vec4(aPos.xy, -aPos.z/6553, 1.0);
    fColor = aColor; // vec4(aPos.xy, 0.0, 1.0);
    fTexCoord = aTexCoord.xy; //aColor.xy;
    fPointSize = 0;
}
