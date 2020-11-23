package io.github.tibequadorian.hgjabba;

public class HGWorldborder {
	
	public static final double DEFAULT_INITIAL_RADIUS = 1000;
	public static final double DEFAULT_FINAL_RADIUS = 50;
	public static final long DEFAULT_START_MOVING_TIME = 3600;
	public static final long DEFAULT_STOP_MOVING_TIME = 18000;
	
	private HGJabba game;
	private double initial_radius;
	private double final_radius;
	private long start_moving_time;
	private long stop_moving_time;
	
	
	public HGWorldborder(HGJabba game) {
		this.game = game;
	}
	
	
	public void start() {
		game.getWorld().getWorldBorder().reset();
		game.getWorld().getWorldBorder().setCenter(0, 0);
		game.getWorldNether().getWorldBorder().reset();
		game.getWorldNether().getWorldBorder().setCenter(0, 0);
		setRadius(initial_radius);
	}
	
	public void pause() {
		double current_radius = getRadius();
		setRadius(current_radius);
	}
	
	public void unpause() {
		double current_radius = getRadius();
		double time = ((current_radius - final_radius) * (start_moving_time - stop_moving_time)) / (final_radius - initial_radius);
		setRadius(final_radius, Math.round(time));
	}
	
	
	public double getRadius() {
		long time = game.getThread().getTime();
		if (time <= start_moving_time) {
			return initial_radius;
		} else if (time >= stop_moving_time) {
			return final_radius;
		} else {
			return (((final_radius - initial_radius) / (stop_moving_time - start_moving_time)) * time) -
					(final_radius * start_moving_time - initial_radius * stop_moving_time) / (stop_moving_time - start_moving_time);
		}
	}
	
	private void setRadius(double radius) {
		setRadius(radius, 0L);
	}
	private void setRadius(double radius, long time) {
		game.getWorld().getWorldBorder().setSize(radius * 2, time);
		game.getWorldNether().getWorldBorder().setSize(radius * 2, time);
	}
	
	// GETTER AND SETTER
	
	public double getInitialRadius() {
		return initial_radius;
	}
	
	public double getFinalRadius() {
		return final_radius;
	}
	
	public long getStartMovingTime() {
		return start_moving_time;
	}
	
	public long getStopMovingTime() {
		return stop_moving_time;
	}
	
	public void setValues(double initial_radius, double final_radius, long start_moving_time, long stop_moving_time) {
		this.initial_radius = initial_radius;
		this.final_radius = final_radius;
		this.start_moving_time = start_moving_time;
		this.stop_moving_time = stop_moving_time;
	}
}
