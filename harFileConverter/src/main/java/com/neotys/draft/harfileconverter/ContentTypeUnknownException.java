package com.neotys.draft.harfileconverter;

public class ContentTypeUnknownException extends Exception {
	/**
	 * Exception used for Content-Type format unknown
	 */
	private static final long serialVersionUID = 5353542622893479298L;
	public ContentTypeUnknownException() { super(); }
	public ContentTypeUnknownException(String message) { super(message); }
	public ContentTypeUnknownException(String message, Throwable cause) { super(message, cause); }
	public ContentTypeUnknownException(Throwable cause) { super(cause); }
}

