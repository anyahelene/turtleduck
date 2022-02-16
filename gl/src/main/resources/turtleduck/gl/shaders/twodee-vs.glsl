
#version 150
#extension GL_ARB_explicit_attrib_location : enable
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 uModel;
uniform mat4 uNormal;
uniform mat4 uProjView;

//out vec4 fPos;
out vec4 fColor;
out vec2 fTexCoord;
out vec4 fNormal;
flat out int fTexNum;

void main()
{
	//float g = (aPos.y + .5)/1.5;
	//float r = -aPos.x/0.866;
	//float b = 1.0 - r;
	
    fTexNum =  int(aPos.z);
  //  fPos = uModel * vec4(aPos.xy,-aPos.z/10, 1.0);
    gl_Position = uProjView * vec4(aPos.xy, -aPos.z/10, 1.0); // uModel * vec4(aPos.xy, 0.0, 1.0);
    fColor = aColor; // vec4(aPos.xy, 0.0, 1.0);
    fTexCoord = aTexCoord.xy; //aColor.xy;
}
