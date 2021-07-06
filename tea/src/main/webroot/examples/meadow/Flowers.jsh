reset();
void sky(Turtle turtle, Color b) {
    for(int i = 0; i < 450; i += 40) {
        turtle.pen(b).fillOnly();
        turtle.draw(1280).turn(90).draw(40).turn(90).draw(1280);
        b = b.brighter();
        turtle.turn(180);
      }
}

void petal(Turtle turtle) {
    turtle.pen(GREEN).penWidth(2);
    turtle.draw(5).turn(-70);
	turtle.pen(MAGENTA.mix(WHITE,.65))
      .strokeAndFill()
      .stroke(c -> c.darker());
    
    for(int i = -90; i < 450; i++) {
        double a = Math.abs(sin(i));
        turtle.turn(a/2).draw(.15).turn(a/2);
        if(i == 135) {
            turtle.turn(12.5);
            i += 90;
        }
    }
}


void flower(Turtle turtle) {
    for(int i = 0; i < 4; i++) {
        petal(turtle.spawn());
        turtle.turn(90);
    }
    turtle.turn(30);
    for(int i = 0; i < 4; i++) {
        turtle.spawn().pen(MAROON).strokeOnly().draw(10);
        turtle.turn(90);
    }
}


void stem(Turtle turtle, int size, int p) {
    turtle.pen(GREEN).stroke(c -> c.darker());
    double a = p*(size/2.0);
    for(int i = size*2; i >= 0; i--) {
        if(i == 7 && size > 4)
            stem(turtle.spawn().turn(-45), size-1, 1);
        turtle.turn(a).draw(i*3+size/2.0);
    }
    if(size > 3)
        flower(turtle.spawn());
    turtle.turn(90).draw(2).turn(90);
    for(int i = 0; i <= size*2; i++) {
         turtle.turn(-a).draw(i*2.8+size/2.0);
         if(i == 7 && size > 4)
            stem(turtle.spawn().turn(-150), size-1, -1);
    }
}

void grass(Turtle turtle) {
    double x = 1280*(Math.random()-.5);
    double y = 300*(Math.random()-.25);
    turtle.jumpTo(x, 100+y).turnTo(-90);
    turtle.turn(5*(Math.random()-.5));
    turtle.draw(70);
    turtle.turn(5).draw(30);
    turtle.turn(170).draw(200);
}

turtle.jumpTo(640,100).turnTo(180);
sky(turtle, BLUE.brighter());
turtle.jumpTo(-640,100).turnTo(0);
sky(turtle, Color.color(.2,.1,.0));
for(int i = 0; i < 1000; i++) {
    grass(turtle.spawn().pen(GREEN.perturb(.15)));
}
turtle.jumpTo(-150,200).turnTo(-90);
stem(turtle, 6, 1);
turtle.jumpTo(150,260).turnTo(-85);
stem(turtle, 5, 1);
turtle.jumpTo(-260,300).turnTo(-95);
stem(turtle, 4, 1);
turtle.jumpTo(360,330).turnTo(-100);
stem(turtle, 7, 1);
for(int i = 0; i < 40; i++) {
    grass(turtle.spawn().pen(GREEN.perturb(.15)));
}
