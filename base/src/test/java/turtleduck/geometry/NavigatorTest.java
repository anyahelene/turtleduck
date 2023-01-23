package turtleduck.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import turtleduck.turtle.Turtle;

public class NavigatorTest {
    Random random = new Random();
    Turtle turtle;
    
    @BeforeEach
    public void setup() {
        turtle = Turtle.create();
    }

    @RepeatedTest(value = 10)
    public void relativeOffset() {
        int angleA = random.nextInt(360), angleB = random.nextInt(360);
        double radB = Math.toRadians(angleB+90);
        Point p1 = turtle.jumpTo(0,0).turnTo(angleA).draw(100).turn(angleB).draw(50).point();
        Point p2 = turtle.jumpTo(0,0).turnTo(angleA).draw(100).drawTo(turtle.offset(50*Math.cos(radB), 50*Math.sin(radB))).point();
        assertEquals(p1.x(), p2.x(), 10e-6);
        assertEquals(p1.y(), p2.y(), 10e-6);
    }

}
