package net.shortround.rose;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class Particle {
	
	private int age;
	private int maxAge;
	
	private Path path;
	private PathMeasure pathMeasure;
	private float x;
	private float y;
	private int opacity;
	
	private Paint paint;
	
	private Interpolator positionInterpolator;
	private Interpolator opacityInterpolator;
	
	Random generator;
	
	public Particle(float startX, float startY, RectF maxBox, int maxAge) {
		// Build the random generator
		generator = new Random();
		
		// Record starting positions
		x = startX;
		y = startY;
		
		// Prepare the age
		age = 0;
		this.maxAge = maxAge;
		
		// Build a path for the particle to travel
		path = buildPath(startX, startY, maxBox);
		pathMeasure = new PathMeasure(path, false);
		
		// Create the interpolators
		positionInterpolator = new DecelerateInterpolator();
		opacityInterpolator = new AccelerateDecelerateInterpolator();
		
		// Build the paint
		paint = new Paint();
		paint.setColor(Color.WHITE);
	}
	
	/*** Drawing ***/
	
	public void draw(Canvas canvas) {
		// Switch to a new matrix
		Matrix oldMatrix = canvas.getMatrix();
		Matrix matrix = new Matrix();
		canvas.setMatrix(matrix);
		
		// Translate to position
		canvas.translate(x, y);
		
		// Draw
		paint.setAlpha(opacity);
		canvas.drawCircle(0, 0, 15.0f, paint);
		
		// Restore the old matrix
		canvas.setMatrix(oldMatrix);
	}
	
	/*** Particle Lifecycle ***/
	
	public void step() {
		// Get older
		age++;
		
		// Calculate drawing values
		float[] xAndY = calculateXandY();
		x = xAndY[0];
		y = xAndY[1];
		opacity = calculateOpacity();
	}
	
	public boolean isDead() {
		return age >= maxAge;
	}
	
	/*** Utilities ***/
	
	private Path buildPath(float startX, float startY, RectF maxBox) {
		// Start from the start point
		Path path = new Path();
		path.moveTo(startX, startY);
		
		// Move to a random point in the max box
		float stopX = maxBox.left + generator.nextFloat() * (maxBox.right - maxBox.left);
		float stopY = maxBox.top + generator.nextFloat() * (maxBox.top - maxBox.bottom);
		path.lineTo(stopX, stopY);
		
		return path;
	}
	
	private int calculateOpacity() {
		float value = opacityInterpolator.getInterpolation(getAgeProgress());
		
		if (value >= 0.50f) value = 1.0f - value;
		
		return (int) (value * 200.0f);
	}
	
	private float[] calculateXandY() {
		
		float distance = pathMeasure.getLength() * positionInterpolator.getInterpolation(getAgeProgress());
		float[] position = new float[2];
		
		pathMeasure.getPosTan(distance, position, null);
		
		return position;
	}
	
	private float getAgeProgress() {
		float fAge = (float) age;
		float fMaxAge = (float) maxAge;
		
		return fAge / fMaxAge;
	}
}