package turtleduck.shapes;

import java.util.function.Consumer;

import turtleduck.colors.Color;
import turtleduck.display.Camera;
import turtleduck.geometry.Point;

/**
 * A particle system, for creating particle effects (like fire, smoke, etc)
 * 
 * @author Anya Helene Bagge
 *
 */
public interface Particles {
	/**
	 * Start the shape at the given point
	 * 
	 * @param p A point
	 * @return this
	 */
	Particles at(Point p);

	/**
	 * Start the shape at the given point
	 * 
	 * @param x Point's x coordinate
	 * @param y Point's y coordinate
	 * @return this
	 */
	Particles at(double x, double y);

	Point position();

	Particles nParticles(int n);

	int nParticles();

	Particles particleSize(double size);

	double particleSize();

	Particles update(Consumer<ParticleTemplate> updater);

	Particles add(Consumer<Particle> updater);

	Particles camera(Camera cam);

	interface Particle {
		Particle size(double start, double end);

		Particle t(double start, double end);

		Particle time(double start, double end);

		Particle time(double interval);

		Particle start(Point p);

		Particle end(Point p);

		Particle ctrl1(Point p);

		Particle ctrl2(Point p);

		Particle color(Color start, Color end);

		Particle color(Color color);

	}

	interface ParticleTemplate extends Particle {
		void save();
	}

	void draw();

}
