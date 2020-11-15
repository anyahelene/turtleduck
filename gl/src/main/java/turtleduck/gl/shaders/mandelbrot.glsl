#version 330 core

in vec4 fColor;
in vec4 fPos;
in vec4 fNormal;
in vec2 fTexCoord;
flat in int fTexNum;
uniform float MAXITER = 1024;
uniform vec2 zoom = vec2(2,2);
uniform vec2 offset = vec2(-1,-1);

out vec4 FragColor;

const float threshold = 0.005;
float equal(float a, float b) {
  float t0 = min(a, b), t1 = max(a, b);
  return t0 > t1-threshold && t0 < t1 ? 1.0 : 0.0;}
float less(float a, float b) { return smoothstep(a-threshold, a, b); }
float greater(float a, float b) { return smoothstep(b-threshold, b, a); }
float between(float a, float b, float c) { return c > min(a,b) && c < max(a,b) ? 1.0 : 0.0; }
void main() {
   vec2 xy0 = zoom*fColor.xy + offset;
   float x = xy0.x, y = xy0.y;
   float i = 0;
   while(x*x+y*y <= 4 && ++i < MAXITER) {
      float tmp = x*x-y*y+xy0.x;
       y = 2*x*y + xy0.y;
       x = tmp;
   }
   float c = max(i-32, 0) / (128-32);
   if(i <= 32) FragColor = mix(vec4(0,0,0,1), vec4(0,0,.1,1), i/32.0);
   else if(i >= 128) FragColor = mix(vec4(1,.9,.4,1), vec4(1,1,1,0), (i-128)/(MAXITER-128));
   else FragColor = mix(vec4(.0,.0,.1,1), vec4(1,.9,.4,1), c);
   if(i >= MAXITER) discard;
       if(FragColor.a < 0.05) discard;
}
