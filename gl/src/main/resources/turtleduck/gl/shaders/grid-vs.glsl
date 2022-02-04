#version 330 core
layout (location = 0) in vec2 aPos;

out VS_OUT {
    vec3 gPos; // position in grid coordinates
    vec4 neighelv; // elevation of neighbours
    vec4 data;  // texture offset
} vs_out;
uniform float size;
uniform vec2 gridSize;
//vec4 gridOffset = vec4(-gridSize/2, 0, 0);

uniform sampler2D texture4;
uniform mat4 uModel;

vec4 getData(vec2 gPos) {
	vec4 data = texelFetch(texture4, ivec2(gPos), 0); // data values in floats, 0..1
	data *= 255; // convert to bytes 0..255
	if(data.z > 127)
		data.z -= 256; // convert to signed
	return data;
}

float getNeighElv(int dx, int dy) {
	vec2 p = clamp(aPos+vec2(dx, dy), vec2(0,0), gridSize-vec2(1,1));
	return getData(p).z;
}

vec4 modelPos(vec3 gPos) {
//	return (gridOffset + vec4(gPos, 1)); // * vec4(1,-1,1,1);
	return vec4(gPos, 1); // * vec4(1,-1,1,1);
}

void main()
{
	vs_out.data = getData(aPos);
    vs_out.gPos = vec3(aPos, vs_out.data.z);
    vec4 mPos = modelPos(vs_out.gPos);
    vs_out.neighelv = vec4(getNeighElv(0,-1), getNeighElv(0,1), getNeighElv(1,0), getNeighElv(-1,0)) - mPos.zzzz;
    gl_Position = uModel * mPos;
}
