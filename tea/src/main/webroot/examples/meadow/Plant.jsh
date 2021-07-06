reset();

turtle.fillColor(TRANSPARENT).penColor(BLUE);

turtle.jumpTo(0,320).turnTo(-90);

void stem(Turtle turtle, int n) {
  if(n <= 0) {
    turtle.turn(90).draw(5).turn(90);
    return;
  }
  turtle.draw(n*40).turn(4);

  if(n > 1) {
    turtle.penColor(MAGENTA);
    turtle.turn(-90).draw(n*20);
    stem(turtle, n-2);
    turtle.penColor(RED);
    turtle.draw(n*20);
    turtle.turn(-90);
  }
  turtle.penColor(GREEN);
  stem(turtle, n-1);
  
  turtle.draw(n*40).turn(4);
}

stem(turtle, 2);
