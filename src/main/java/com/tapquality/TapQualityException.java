package com.tapquality;

public class TapQualityException extends Exception {
	private static final long serialVersionUID = 1L;

	public TapQualityException(String message, Exception e) {
		super(message, e);
	}

	public TapQualityException(String message) {
		super(message);
	}

}
