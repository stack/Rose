package net.shortround.rose;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.RectF;

public class ParticleSystem {
	// Particle constants
	private static final int MAX_PARTICLES = 10;
	
	// Age constants
	private static final int MIN_AGE = 100;
	private static final int MAX_AGE = 120;
	
	// Box constants
	public static final int TOP = 0;
	public static final int LEFT = 1;
	public static final int BOTTOM = 2;
	public static final int RIGHT = 3;
	
	private CopyOnWriteArrayList<Particle> particles;
	
	private RectF generationBox;
	private RectF maxBox;
	
	private Random generator;
	
	private boolean running;
	
	public ParticleSystem(RectF generationBox, RectF maxBox) {
		// Build the list of particles
		particles = new CopyOnWriteArrayList<Particle>();
		
		// Set up the boxes
		this.generationBox = generationBox;
		this.maxBox = maxBox;
		
		// Set up the random number generator
		generator = new Random();
	}
	
	public void start() {
		// Clear the particles and add new ones
		particles.clear();
		
		// Mark that we are running
		running = true;
	}
	
	public void step() {
		ArrayList<Particle> deceased = new ArrayList<Particle>();
		
		// Add a particle if we don't have enough
		if (particles.size() < MAX_PARTICLES) {
			particles.add(createParticle());
		}
		
		// Age the particles
		for (Particle particle : particles) {
			particle.step();
			
			// Mark invalids for death
			if (particle.isDead()) {
				deceased.add(particle);
			}
		}
		
		// Death and Birth
		for (Particle particle : deceased) {
			// Kill the old particle
			particles.remove(particle);
			
			// Add a new particle
			particles.add(createParticle());
		}
	}
	
	public void stop() {
		// Clear the particles
		particles.clear();
				
		// Mark that we are not running
		running = false;
	}
	
	public void changeBoxes(RectF generationBox, RectF maxBox) {
		// Don't change boxes if we are running
		if (running) return;
		
		this.generationBox = generationBox;
		this.maxBox = maxBox;
	}
	
	private Particle createParticle() {
		// Create starting point
		float startX = generationBox.left + generator.nextFloat() * (generationBox.right - generationBox.left);
		float startY = generationBox.top + generator.nextFloat() * (generationBox.bottom - generationBox.top);
		
		// Create an age
		int age = MIN_AGE + generator.nextInt(MAX_AGE - MIN_AGE);
		
		// Create the particle
		Particle particle = new Particle(startX, startY, maxBox, age);
		
		return particle;
	}
	
	public CopyOnWriteArrayList<Particle> getParticles() {
		return particles;
	}
	
	public boolean isRunning() {
		return running;
	}
}
