package com.neotys.draft.harfileconverter;

public class ContentTypeException extends Exception {
	/**
	 * Exception used for Content-Type format unknown
	 */
	private static final long serialVersionUID = 5353542622893479298L;
	public ContentTypeException() { super(); }
	public ContentTypeException(String message) { super(message); }
	public ContentTypeException(String message, Throwable cause) { super(message, cause); }
	public ContentTypeException(Throwable cause) { super(cause); }
}

