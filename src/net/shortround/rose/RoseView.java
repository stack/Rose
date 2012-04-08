package net.shortround.rose;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class RoseView extends View {
	// Debug
	private static final String TAG = "RoseView";

	public static final int MIN_DECAY = 0;
	public static final int MAX_DECAY = 14;
	
	private static final float ROSE_WIDTH = 575.0f;
	private static final float ROSE_HEIGHT = 700.0f;
	
	private boolean animating;
	private boolean display;
	
	private int battery;
	private int decay;
	private float scale;
	
	private Bitmap singlePetal;
	private Bitmap petal;
	private Bitmap stem;
	private ArrayList<Integer> petals;
	private ArrayList<Integer> stems;
	
	private Path path;
	private PathMeasure pathMeasure;
	private float petalOffset;
	
	public RoseView(Context context) {
		super(context);
		
		// Set some defaults
		animating = false;
		battery = 0;
		decay = 0;
		display = true;
		scale = 1.0f;
		
		// Build the petals
		petals = new ArrayList<Integer>();
		petals.add(new Integer(R.drawable.petals01));
		petals.add(new Integer(R.drawable.petals02));
		petals.add(new Integer(R.drawable.petals03));
		petals.add(new Integer(R.drawable.petals04));
		petals.add(new Integer(R.drawable.petals05));
		petals.add(new Integer(R.drawable.petals06));
		petals.add(new Integer(R.drawable.petals07));
		petals.add(new Integer(R.drawable.petals08));
		petals.add(new Integer(R.drawable.petals09));
		petals.add(new Integer(R.drawable.petals10));
		petals.add(new Integer(R.drawable.petals11));
		petals.add(new Integer(R.drawable.petals12));
		petals.add(new Integer(R.drawable.petals13));
		petals.add(new Integer(R.drawable.petals14));
		petals.add(new Integer(R.drawable.petals15));
		
		// Build the petals
		stems = new ArrayList<Integer>();
		stems.add(new Integer(R.drawable.stems01));
		stems.add(new Integer(R.drawable.stems02));
		stems.add(new Integer(R.drawable.stems03));
		stems.add(new Integer(R.drawable.stems04));
		stems.add(new Integer(R.drawable.stems05));
		stems.add(new Integer(R.drawable.stems06));
		stems.add(new Integer(R.drawable.stems07));
		stems.add(new Integer(R.drawable.stems08));
		stems.add(new Integer(R.drawable.stems09));
		stems.add(new Integer(R.drawable.stems10));
		stems.add(new Integer(R.drawable.stems11));
		stems.add(new Integer(R.drawable.stems12));
		stems.add(new Integer(R.drawable.stems13));
		stems.add(new Integer(R.drawable.stems14));
		stems.add(new Integer(R.drawable.stems15));
		
		clearStaticAssets();
		clearAnimationAssets();
	}
	
	private void clearStaticAssets() {
		// Clear the bitmaps
		singlePetal = null;
		petal = null;
		stem = null;
	}
	
	private void clearAnimationAssets() {
		path = null;
		pathMeasure = null;
		petalOffset = 0.0f;
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
		clearStaticAssets();
		
		// Start the animation
		ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
		animator.setDuration(2000);
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		
		animator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animator) {
				animating = false;
				clearAnimationAssets();
				invalidate();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				animating = false;
				clearAnimationAssets();
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
		clearStaticAssets();
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
			// Draw the stem
			drawStem(canvas);
			
			// If animating, draw the single petal
			if (animating) {
				drawSinglePetal(canvas);
			}
			
			// Draw the petal
			drawPetal(canvas);
		}
	}
	
	private void drawSinglePetal(Canvas canvas) {
		canvas.save();
		
		// Get the animation path and move to the right path location
		getPath(canvas);
		float distance = pathMeasure.getLength() * petalOffset;
		float[] position = new float[2];
		
		pathMeasure.getPosTan(distance, position, null);
		
		// Set the proper offset and scale
		canvas.translate(position[0], position[1]);
		if (decay % 2 == 0) {
			canvas.scale(scale, scale);
		} else {
			canvas.scale(scale * -1.0f, scale);
		}
								
		// Draw the appropriate rose
		Bitmap singlePetal = getSinglePetal();
		canvas.drawBitmap(singlePetal, singlePetal.getWidth() / -2, singlePetal.getHeight() * -1, null);
		
		canvas.restore();
	}
	
	private void drawPetal(Canvas canvas) {
		canvas.save();
		
		// Set the proper offset and scale
		canvas.translate(canvas.getWidth() / 2, canvas.getHeight());
		canvas.scale(scale, scale);
						
		// Draw the appropriate rose
		Bitmap petal = getPetal();
		canvas.drawBitmap(petal, petal.getWidth() / -2, petal.getHeight() * -1, null);
		
		canvas.restore();
	}
	
	private void drawStem(Canvas canvas) {		
		canvas.save();
		
		// Set the proper offset and scale
		canvas.translate(canvas.getWidth() / 2, canvas.getHeight());
		canvas.scale(scale, scale);
						
		// Draw the appropriate rose
		Bitmap stem = getStem();
		canvas.drawBitmap(stem, stem.getWidth() / -2, stem.getHeight() * -1, null);
		
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
				RoseActivity activity = (RoseActivity) getContext();
				activity.ensureDiscoverable();
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
	
	private Bitmap getSinglePetal() {
		if (singlePetal == null) {
			singlePetal = BitmapFactory.decodeResource(getResources(), R.drawable.petal);
		}
		
		return singlePetal;
	}
	
	private Bitmap getPetal() {
		if (petal == null) {
			int resource = petals.get(decay).intValue();
			petal = BitmapFactory.decodeResource(getResources(), resource);
		}
		
		return petal;
	}
	
	private Bitmap getStem() {
		if (stem == null) {
			int resource = stems.get(decay).intValue();
			stem = BitmapFactory.decodeResource(getResources(), resource);
		}
		
		return stem;
	}
	
	public JSONObject getSerializedData() {
		JSONObject json = new JSONObject();
    	
    	try {
    		json.put("decay", getDecay());
    		json.put("max_decay", RoseView.MAX_DECAY);
    		json.put("battery", getBattery());
    		json.put("display", getDisplay());
    	} catch (JSONException e) {
    		Log.d(TAG, "JSON creation failed", e);
    	}
    	
    	return json;
	}
	
	public void setBattery(int value) {
		battery = value;
	}

}
