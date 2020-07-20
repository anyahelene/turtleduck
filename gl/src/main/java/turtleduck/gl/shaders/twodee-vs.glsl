
#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;

uniform mat4 uModel;
uniform mat4 uNormal;
uniform mat4 uProjView;

out vec2 fTexCoord;
out vec4 fPos;
out vec4 fColor;
out vec4 fNormal;
out flat int fTexNum;

void main()
{
	//float g = (aPos.y + .5)/1.5;
	//float r = -aPos.x/0.866;
	//float b = 1.0 - r;
	
    fPos = uModel * vec4(aPos.xy, 0.0, 1.0);
    gl_Position = uProjView * fPos; // uModel * vec4(aPos.xy, 0.0, 1.0);
    fColor = aColor; // vec4(aPos.xy, 0.0, 1.0);
    fTexCoord = aColor.xy;
    fTexNum = int(aPos.z);
}
