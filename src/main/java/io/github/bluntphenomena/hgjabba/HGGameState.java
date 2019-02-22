package io.github.bluntphenomena.hgjabba;

public enum HGGameState {
	
	PREGAME(false),
	WAITING(true),
	WAITING_FALLBACK(true),
	RUNNING(false),
	RUNNING_FALLBACK(false);
	
	
	private boolean idle;
	
	
	private HGGameState(boolean idle) {
		this.idle = idle;
	}
	
	
	public boolean isIdle() {
		return idle;
	}
}
