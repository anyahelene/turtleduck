reset();
turtle.penWidth(5);
turtle.jumpTo(0,0);
//turtle.penColor(Color.hsv(i, 1, 1));

void petal(Turtle turtle) {
  int a = 45;
  turtle.fillColor(YELLOW.perturb())
    .penWidth(1)
    .penColor(YELLOW.darker());
  turtle.turn(-3*a);
  for(int i = a; i < 360 - a; i++) {
    turtle.draw(.25).turn(1);
    if(i == 180)
      turtle.turn(a);
  }
}
void flower(Turtle turtle) {
  for(int i = 0; i < 5; i++) {
  	petal(turtle.turn(72).spawn().draw(5));
  }
  turtle.penColor(MAROON.darker()).penWidth(1);
  for(int i = 0; i < 5; i++) {
  	turtle.turn(72).spawn().draw(15);
  }  
}

void background(Turtle turtle) {
  turtle.jumpTo(-640,40);
  for(int i = 0; i < 20; i++) {
    turtle.fillColor(BLUE.mix(WHITE,i/20.0)).penColor(TRANSPARENT);
    turtle.turnTo(0).draw(1280)
      .turn(-90).draw(20)
      .turn(-90).draw(1280);
  }
  turtle.jumpTo(-640,40);
  for(int i = 0; i < 20; i++) {
    turtle.fillColor(Color.hsv(20,.5,.4).mix(BLACK,(20-i)/20.0)).penColor(TRANSPARENT);
    turtle.turnTo(0).draw(1280)
      .turn(90).draw(20)
      .turn(90).draw(1280);
  }
}  
void stem(Turtle turtle, int n, Color c) {
	if(n <= 0)
    	return;
	turtle.penColor(c).draw(50*n).turn(rand(-5,5));
  	if(n == 1) {
		flower(turtle);
    } else {
    	stem(turtle.spawn().turn(-rand(30,45)),
             n-randInt(1,3), c.brighter());
    	stem(turtle.spawn(), n-1, c.brighter());
      	stem(turtle.spawn().turn(rand(30,45)),
             n-randInt(1,3), c.brighter());
    }
}  

background(turtle.spawn());
turtle.jumpTo(70,-120).penColor(null);
turtle.jump(50).turn(90);
for(int i = 0; i < 360; i+=1) {
  turtle.spawn().fillColor(YELLOW.perturb().opacity(.5)).turn(-88.5).draw(155).turn(175).draw(165).done();
  turtle.turn(3.7);
}
void grass(Turtle turtle) {
  turtle.penColor(null).fillColor(GREEN.perturb());
  double a = rand(10)+5;
  turtle.draw(100).turn(a)
    .draw(20).turn(180-a)
    .draw(120);
}  
for(int i = 0; i < 2000; i++) {
  grass(turtle.spawn().turnTo(-90).turn(rand(5))
        .jumpTo(rand(1280)-640, rand(50, 400)));
}
for(int i = 0; i < 16; i++) {
	stem(turtle.spawn()
         .jumpTo(-600+1200*rand(), rand(200)+200)
         .turnTo(-90),
      randInt(2,5), GREEN);
}  
for(int i = 0; i < 500; i++) {
  grass(turtle.spawn().turnTo(-90).turn(rand(5))
        .jumpTo(rand(1280)-640, rand(250, 400)));
}
