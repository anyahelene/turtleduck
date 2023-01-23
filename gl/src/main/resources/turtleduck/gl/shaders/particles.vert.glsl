#version 300 es
layout (location = 0) in vec4 startPos;
layout (location = 1) in vec4 startColor;
layout (location = 2) in vec2 lifetime;
layout (location = 3) in vec4 endPos;
layout (location = 4) in vec4 endColor;
layout (location = 5) in vec2 size;
layout (location = 6) in vec2 len;
layout (location = 7) in vec2 depth;

uniform mat4 uModel;
uniform mat4 uNormal;
uniform mat4 uProjView;
uniform vec2 uViewPort;
uniform float uTime;
uniform float uZ;
//#ifdef GL_ES
uniform highp float uPointScale;
//#endif

out TwoDeeFrag {
	vec4 fColor;
	vec2 fTexCoord;
	vec4 fNormal;
	flat int fTexNum;
};

vec2 interpolateBezier(vec2 src, vec2 ctrl1, vec2 ctrl2, vec2 dest, float f) {
	vec2 p1 = mix(src, ctrl2, f);
	vec2 p2 = mix(ctrl1, ctrl2, f);
	vec2 p3 = mix(ctrl2, dest, f);
	vec2 p4 = mix(p1, p2, f);
	vec2 p5 = mix(p2, p3, f);
	vec2 p6 = mix(p4, p5, f);
	return p6;
}

void main() {
//#ifdef GL_ES
//#endif

	fTexCoord = vec2(0, 0);
	if (uTime >= lifetime.x && uTime < lifetime.y) {
		float todo = lifetime.y - lifetime.x;
		float done = lifetime.y - uTime;
		float lifeStage = done / todo;
		fColor = mix(startColor, endColor, sin(lifeStage * 1.57));
		;
		//fColor.a *= .25;
		//fColor *= sin(lifeStage*1.57);
		//fColor /= 2;
		//fColor.a = 1;
		fTexCoord.x = lifeStage;
		fTexCoord.y = sin(lifeStage * 1.57);
		gl_PointSize = (uPointScale * mix(size.y, size.x, sin(lifeStage * 1.57)));
		if (gl_PointSize < 0.0001) {
			gl_Position = vec4(10, 10, 10, 1);
			fColor = vec4(0);
			fTexNum = 0;
		} else if (true) {
			float bpos = mix(len.y, len.x, lifeStage);
			vec2 pos = interpolateBezier(startPos.xy, startPos.zw, endPos.xy,
					endPos.zw, bpos);
			gl_Position = uProjView * uModel
					* vec4(pos, mix(depth.x, depth.y, lifeStage), 1.0);
			vec4 pos2 = uProjView * uModel
					* vec4(pos+640.0*vec2(gl_PointSize,gl_PointSize), mix(depth.x, depth.y, lifeStage), 1.0);
			gl_PointSize = uProjView * gl_PointSize * uViewPort.x; //length(pos2-gl_Position);
			gl_Position.z = uZ;
		} else {
			vec2 offset = endPos.xy * (1.0 - lifeStage);
			gl_Position = uProjView
					* vec4(startPos.xy + offset, startPos.z, 1.0);
		}
		fTexNum = 0;
	} else {
		gl_Position = vec4(10, 10, 10, 1);
		fColor = vec4(0);
		fTexNum = 0;
	}
//	gl_Position = uProjView * vec4(startPos.xy,0,1);
	fTexNum = 0;
}
