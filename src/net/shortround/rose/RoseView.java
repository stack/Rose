package net.shortround.rose;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class RoseView extends View {

	public static final int MIN_DECAY = 0;
	public static final int MAX_DECAY = 14;
	
	private static final float ROSE_WIDTH = 575.0f;
	private static final float ROSE_HEIGHT = 700.0f;
	
	private boolean animating;
	private boolean display;
	
	private int battery;
	private int decay;
	private float scale;
	
	private Path path;
	private PathMeasure pathMeasure;
	private Bitmap petal;
	private Bitmap rose;
	private ArrayList<Integer> roses;
	
	private float petalOffset;
	
	public RoseView(Context context) {
		super(context);
		
		// Set some defaults
		animating = false;
		battery = 0;
		decay = 0;
		display = true;
		scale = 1.0f;
		
		// Build the roses
		roses = new ArrayList<Integer>();
		roses.add(new Integer(R.drawable.rose_01));
		roses.add(new Integer(R.drawable.rose_02));
		roses.add(new Integer(R.drawable.rose_03));
		roses.add(new Integer(R.drawable.rose_04));
		roses.add(new Integer(R.drawable.rose_05));
		roses.add(new Integer(R.drawable.rose_06));
		roses.add(new Integer(R.drawable.rose_07));
		roses.add(new Integer(R.drawable.rose_08));
		roses.add(new Integer(R.drawable.rose_09));
		roses.add(new Integer(R.drawable.rose_10));
		roses.add(new Integer(R.drawable.rose_11));
		roses.add(new Integer(R.drawable.rose_12));
		roses.add(new Integer(R.drawable.rose_13));
		roses.add(new Integer(R.drawable.rose_14));
		roses.add(new Integer(R.drawable.rose_15));
		
		path = null;
		pathMeasure = null;
		petalOffset = 0.0f;
		rose = null;
	}
	
	/*** External Commands ***/
	public void decay() {
		// Fail if we're animating
		if (animating) {
			return;
		}
		
		// Check the decay level
		if (decay >= MAX_DECAY) {
			return;
		}
		
		// Set the decay
		decay += 1;
		rose = null;
		
		// Start the animation
		ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
		animator.setDuration(2000);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		animator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animator) {
				animating = false;
				
				path = null;
				pathMeasure = null;
				petal = null;
				
				invalidate();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				animating = false;

				path = null;
				pathMeasure = null;
				petal = null;
				
				invalidate();
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}

			@Override
			public void onAnimationStart(Animator animation) {
				animating = true;
			}
		});
		
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				petalOffset = animation.getAnimatedFraction();
				invalidate();
			}
		});
		
		animator.start();
	}
	
	public void revert() {
		// Fail if we're animating
		if (animating) {
			return;
		}
				
		// Check the decay level
		if (decay <= MIN_DECAY) {
			return;
		}
		
		// Just set the decay
		decay -= 1;
		rose = null;
		invalidate();
	}
	
	public void toggleDisplay() {
		// Fail if we're animating
		if (animating) {
			return;
		}
		
		// Toggle
		display = display ? false : true;
		invalidate();
	}
	
	/*** View Callbacks ***/
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Draw the background
		Paint backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(Color.BLACK);
		canvas.drawPaint(backgroundPaint);

		// Only draw if we are displaying
		if (display) {
			// If animating, draw the petal
			if (animating) {
				drawPetal(canvas);
			}
			
			// Draw the rose
			drawRose(canvas);
		}
	}
	
	private void drawPetal(Canvas canvas) {
		canvas.save();
		
		// Get the animation path and move to the right path location
		getPath(canvas);
		float distance = pathMeasure.getLength() * petalOffset;
		float[] position = new float[2];
		
		pathMeasure.getPosTan(distance, position, null);
		
		// Set the proper offset and scale
		canvas.translate(position[0], position[1]);
		canvas.scale(scale, scale);
								
		// Draw the appropriate rose
		Bitmap petal = getPetal();
		canvas.drawBitmap(petal, petal.getWidth() / -2, petal.getHeight() * -1, null);
		
		canvas.restore();
	}
	
	private void drawRose(Canvas canvas) {
		canvas.save();
		
		// Set the proper offset and scale
		canvas.translate(canvas.getWidth() / 2, canvas.getHeight());
		canvas.scale(scale, scale);
						
		// Draw the appropriate rose
		Bitmap rose = getRose();
		canvas.drawBitmap(rose, rose.getWidth() / -2, rose.getHeight() * -1, null);
		
		canvas.restore();
	}
	
	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		// Set up a new scale
		float scaleX = (float) width / ROSE_WIDTH;
		float scaleY = (float) height / ROSE_HEIGHT;
		
		scale = scaleX > scaleY ? scaleY : scaleX;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Switch back to Low Profile mode, just in case
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// A touch in the top-left enabled discovery
			if (event.getX() < 50.0f && event.getY() < 50.0f) {
				// Become discoverable here
				return true;
			}
			
			// A touch has ended, split the screen and change the decay for the stem
			if (event.getX() < getWidth() / 2.0f) {
				revert();
				return true;
			} else {
				decay();
				return true;
			}
		}

		// The event wasn't handled, so return false
		return false;
	}
	
	/*** Properties ***/
	
	public int getBattery() {
		return battery;
	}
	
	public int getDecay() {
		return decay;
	}
	
	public boolean getDisplay() {
		return display;
	}
	
	private Path getPath(Canvas canvas) {
		if (path == null) {
			path = new Path();
			path.moveTo(canvas.getWidth() / 2, canvas.getHeight());
			path.quadTo(canvas.getWidth() / 2, canvas.getHeight() + ROSE_HEIGHT * 1.5f, canvas.getWidth(), canvas.getHeight() + ROSE_HEIGHT * 1.5f);
			pathMeasure = new PathMeasure(path, false);
		}
		
		return path;
	}
	
	private Bitmap getPetal() {
		if (petal == null) {
			petal = BitmapFactory.decodeResource(getResources(), R.drawable.petal);
		}
		
		return petal;
	}
	
	private Bitmap getRose() {
		if (rose == null) {
			int resource = roses.get(decay).intValue();
			rose = BitmapFactory.decodeResource(getResources(), resource);
		}
		
		return rose;
	}
	
	public void setBattery(int value) {
		battery = value;
	}

}
