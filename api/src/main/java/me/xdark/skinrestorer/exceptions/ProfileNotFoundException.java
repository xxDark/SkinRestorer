package me.xdark.skinrestorer.exceptions;

public final class ProfileNotFoundException extends Exception {
	public ProfileNotFoundException() { }

	public ProfileNotFoundException(String message) {
		super(message);
	}

	public ProfileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProfileNotFoundException(Throwable cause) {
		super(cause);
	}

	public ProfileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
