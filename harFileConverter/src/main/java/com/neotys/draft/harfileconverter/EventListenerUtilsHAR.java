package com.neotys.draft.harfileconverter;

import com.neotys.neoload.model.listener.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a global EventListenerUtil containing the Neoload EventListener class (classic pattern design Observer).
 * This class will be created in the HAR Reader/Converter to inform the registered listener of the events occuring in HAR Converter...
 *
 */
public class EventListenerUtilsHAR {

	//Attributs
	private static final List<EventListener> listEventListeners = new ArrayList<>();

	//Constructor
	private EventListenerUtilsHAR() {
	}

	//Methods
	public static void addEventListener(EventListener eventlistener) {
		if (eventlistener != null) { 
			listEventListeners.add(eventlistener);
		}
	}


	public static void readSupportedAction(final String actionName) {
		listEventListeners.forEach(listener -> listener.readSupportedAction(actionName));
	}

	public static void readSupportedParameterWithWarn(final String scriptName, final String parameterType, final String parameterName, final String warning) {
		listEventListeners.forEach(listener -> listener.readSupportedParameterWithWarn(scriptName, parameterType, parameterName, warning));
	}

	public static void readUnsupportedParameter(final String scriptName, final String parameterType, final String parameterName) {
		listEventListeners.forEach(listener -> listener.readUnsupportedParameter(scriptName, parameterType, parameterName));
	}

	public static void readSupportedFunction(final String scriptName, final String functionName) {
		listEventListeners.forEach(listener -> listener.readSupportedFunction(scriptName, functionName, 0));
	}

	public static void readUnsupportedFunction(final String scriptName, final String functionName) {
		listEventListeners.forEach(listener -> listener.readUnsupportedFunction(scriptName, functionName, 1));
	}

	public static void readSupportedFunctionWithWarn(final String scriptName, final String functionName, final String warning) {
		listEventListeners.forEach(listener -> listener.readSupportedFunctionWithWarn(scriptName, functionName, 0, warning));
	}

	public static void readUnsupportedAction(String s) {
		listEventListeners.forEach(listener -> listener.readUnsupportedAction(s));
	}

	static void endReadingScripts() {
		listEventListeners.forEach(listener -> listener.endReadingScripts());
	}

	static void startReadingScripts(final int ligne) {
		listEventListeners.forEach(listener -> listener.startReadingScripts(ligne));
	}

	static void endScript() {
		listEventListeners.forEach(listener -> listener.endScript());
	}

	static void startScript(String name) {
		listEventListeners.forEach(listener -> listener.startScript(name));
	}
}