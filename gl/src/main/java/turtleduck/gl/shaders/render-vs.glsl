
#version 430 core
layout (location = 0) in vec3 aPos;
out vec2 fTexCoord;

void main()
{
	gl_Position = vec4(aPos,1);
	fTexCoord = (aPos.xy + 1) / 2;
}
