package com.tapquality.dao;

import com.tapquality.TapQualityException;

public class TapQualityDataException extends TapQualityException {
	private static final long serialVersionUID = 1L;

	public TapQualityDataException(String message, Exception e) {
		super(message, e);
	}

	public TapQualityDataException(String message) {
		super(message);
	}

}
