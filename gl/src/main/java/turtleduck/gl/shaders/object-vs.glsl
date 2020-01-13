
#version 430 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec3 aNormal;
layout (location = 3) in vec2 aTexCoord;

uniform mat4 uModel;
uniform mat4 uNormal;
uniform mat4 uProjView;

out vec2 fTexCoord;
out vec4 fPos;
out vec4 fColor;
out vec4 fNormal;
out vec2 fGridCoord;
out vec3 fId;
void main()
{
    fPos = uModel * vec4(aPos.xyz, 1.0);
    gl_Position = uProjView * fPos; // uModel * vec4(aPos.xyz, 1.0);
    fColor = vec4(aColor,1.0); // vec4(aPos.xyz, 1.0);
    fTexCoord = vec2(0,0);
    fNormal =  uNormal * vec4(normalize(aNormal),0);
    fGridCoord = vec2(-1,-1);
}
