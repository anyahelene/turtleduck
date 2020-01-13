#version 330 core
layout (points) in;
layout (triangle_strip, max_vertices = 20) out;

in VS_OUT {
    vec3 gPos; // position in grid coordinates
    vec4 neighelv; // elevation of neighbours
    vec4 data;  // texture offset
} gs_in[];

uniform mat4 uModel;
uniform mat4 uNormal;
//uniform mat4 uView;
//uniform mat4 uProjection;
uniform mat4 uProjView;
uniform float size;
uniform vec2 cellSize;
uniform vec2 tileSize;
uniform vec2 gridSize;
uniform vec3 selection;
uniform float time;

out vec2 fTexCoord;
out vec4 fColor;
out vec4 fPos;
out vec4 fNormal;
out vec3 fId;
out vec2 fGridCoord;
out float fSelected;
out float fAux;
void emit(vec4 pos, vec2 tex, vec2 grid) {
	fPos = pos;
	gl_Position = uProjView * fPos; // (fPos + fNormal*abs(time)/10);
	fGridCoord = grid;
	fTexCoord = tex;
	EmitVertex();
}
void emit(vec4 pos, vec2 tex) {
	emit(pos, tex, vec2(0,0));
}

void main() {
	fColor = vec4(0,0,0,0);
	vec4 pos = gl_in[0].gl_Position;
	vec4 vPos = uProjView * pos;
	vPos /= vPos.w;
//	if(vPos.x < -1.1 || vPos.x > 1.1 || vPos.y < -1.1 || vPos.y > 1.1)
//		return;
	fAux = gs_in[0].data.w;
	vec2 tex = gs_in[0].data.xy*tileSize;

	vec2 texNW = tex + vec2(0,0);
	vec2 texNE = tex + vec2(tileSize.x, 0);
	vec2 texSW = tex + vec2(0,tileSize.y);
	vec2 texSE = tex + tileSize;

	vec2 gridMid = ((gs_in[0].gPos.xy+vec2(.5,.5)))/gridSize;
	vec2 gridNW = gridMid + vec2(-.5,.5)/gridSize;
	vec2 gridNE = gridMid + vec2(.5,.5)/gridSize;
	vec2 gridSW = gridMid + vec2(-.5,-.5)/gridSize;
	vec2 gridSE = gridMid + vec2(.5,-.5)/gridSize;

    vec4 posSW = pos + uModel * vec4(-cellSize.x, -cellSize.y, 0, 0);
    vec4 posSE = pos + uModel * vec4(cellSize.x, -cellSize.y, 0, 0);
    vec4 posNW = pos + uModel * vec4(-cellSize.x, cellSize.y, 0, 0);
    vec4 posNE = pos + uModel * vec4(cellSize.x, cellSize.y, 0, 0);

    fSelected = gs_in[0].gPos.xy == selection.xy ? 1.0 : 0.0;

    // TOP
    fId = vec3(gs_in[0].gPos.xy, 0);
    fNormal = uNormal * vec4(0,0,1,0);
    emit(posSW, texSW, gridSW);
    emit(posSE, texSE, gridSE);
    emit(posNW, texNW, gridNW);
    emit(posNE, texNE, gridNE);
	EndPrimitive();

    float other = gs_in[0].neighelv.x;
    if(other < 0) { // NORTH
    	fNormal = uNormal * vec4(0,-1,0,0);
        fId.z = 2;
        emit(posSE, texNE, gridSE);
        emit(posSW, texNW, gridSW);
        emit(posSE+uModel * vec4(0,0,other,0), texSE, gridSE);
        emit(posSW+uModel * vec4(0,0,other,0), texSW, gridSW);
        EndPrimitive();
    }

    other = gs_in[0].neighelv.y;
    if(other < 0) { // SOUTH
    	fNormal = uNormal * vec4(0,1,0,0);
        fId.z = 3;
        emit(posNE, texNE, gridNE);
        emit(posNE+uModel * vec4(0,0,other,0), texSE, gridNE);
        emit(posNW, texNW, gridNW);
        emit(posNW+uModel * vec4(0,0,other,0), texSW, gridNW);
        EndPrimitive();
    }

    other = gs_in[0].neighelv.z;
    if(other < 0) { // EAST
		fNormal = uNormal * vec4(1,0,0,0);
        fId.z = 1;
        emit(posNE, texNW, gridNE);
        emit(posSE, texNE, gridSE);
        emit(posNE+uModel * vec4(0,0,other,0), texSW, gridNE);
        emit(posSE+uModel * vec4(0,0,other,0), texSE, gridSE);
        EndPrimitive();
    }

    other = gs_in[0].neighelv.w;
    if(other < 0) { // WEST
    	fNormal = uNormal * vec4(-1,0,0,0);
        fId.z = 4;
        emit(posNW, texNW, gridNW);
        emit(posNW+uModel * vec4(0,0,other,0), texSW, gridNW);
        emit(posSW, texNE, gridSW);
        emit(posSW+uModel * vec4(0,0,other,0), texSE, gridSW);
        EndPrimitive();
    }

}
