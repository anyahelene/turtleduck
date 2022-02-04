
#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec2 aTexCoord;
layout (location = 4) in vec2 aGridCoord;

uniform mat4 uModel;
uniform mat4 uNormal;
uniform mat4 uProjView;

out vec2 fTexCoord;
out vec4 fPos;
out vec4 fColor;
out vec4 fNormal;

void main()
{
	float g = (aPos.y + .5)/1.5;
	float r = -aPos.x/0.866;
	float b = 1.0 - r;
    fPos = uModel * vec4(aPos.xyz, 1.0);
    gl_Position = uProjView * fPos; // uModel * vec4(aPos.xyz, 1.0);
    fColor = vec4(aColor); // vec4(aPos.xyz, 1.0);
    fTexCoord = aTexCoord;
    //fNormal = vec4(mat3(transpose(inverse(uProjView))) * mat3(transpose(inverse(uModel))) * (aNormal),0);
    fNormal = uModel * vec4(aNormal,0);
}
