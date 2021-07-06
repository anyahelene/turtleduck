reset();
void petal(Turtle turtle, int a, double s) { 
  turtle.turn(-a); 
  for(int i = a; i < 360-a; i++) {
    turtle.draw(s).turn(1); 
    if(i == 180)
      turtle.turn(a); 
    }
  }

void flower(Turtle turtle) { 
  turtle.fillColor(YELLOW.perturb()).penColor(TRANSPARENT); 
  for(int i = 0; i < 5; i++) {  
    petal(turtle.turn(72).spawn().jump(1), 65, .25);  	
    turtle.spawn()
      .penColor(MAROON) 
      .penWidth(1)  
      .draw(5);
    }
  }	
void stem(Turtle turtle, int n) { 
  turtle.penColor(GREEN).fillColor(TRANSPARENT); 
  if(n <= 0) { 
    return; 
    }  
  turtle.penWidth(n); 
  turtle.draw(45*n);  
  if(n > 1) {
    stem(turtle.spawn().turn(-45), n-2);
    stem(turtle.spawn().turn(rand(-5,5)), n-1);
    stem(turtle.spawn().turn(45), n-1); 
    } else {
    flower(turtle.spawn());  
    }
  }

turtle.jumpTo(0, 320).turnTo(-90);
stem(turtle, 4);
turtle.jumpTo(0,150).turnTo(-90);

void wing(SpriteBuilder turtle, int a, double s, int mirror) {
  turtle.turn(-a/2);
  for(int i = a; i < 360+a; i++) {  
    turtle.draw(s).turn(1*mirror);  
    if(i == 180)   
      turtle.turn(a*mirror);  
    else if(i == 240)   
      turtle.turn(-2*a*mirror);  
    else if(i == 300)     
      turtle.turn(a*mirror);  
    }
  }

Sprite butterfly(Turtle turtle) { 
  SpriteBuilder sb = turtle.sprite(); 
  sb.fillColor(PURPLE).penColor(WHITE); 
  wing(sb.spawn().draw(150).penColor(null), 45, 2, 1); 
  wing(sb.spawn().draw(150).penColor(null), 45, 2, -1); 
  return sb.done();
  }  
