package com.neotys.draft.harfileconverter;

import com.neotys.neoload.model.listener.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class EventListenerUtil contains the event listeners (pattern design: Observer).
 * This class will be created in the HAR Reader/Converter to inform the registered listener of the events occuring in HAR Converter...
 *
 */
public class EventListenerUtilsHAR {

	//Attributs
	private List<EventListener> listEventListeners = new ArrayList<>();

	//Constructor
	public EventListenerUtilsHAR() {
	}

	//Methods
	public void addEventListener(EventListener eventlistener) {
		if (eventlistener != null) { 
			listEventListeners.add(eventlistener);
		}
	}


	public void readSupportedAction(final String actionName) {
		listEventListeners.forEach(listener -> listener.readSupportedAction(actionName));
	}

	public void readSupportedParameterWithWarn(final String scriptName, final String parameterType, final String parameterName, final String warning) {
		listEventListeners.forEach(listener -> listener.readSupportedParameterWithWarn(scriptName, parameterType, parameterName, warning));
	}

	public void readUnsupportedParameter(final String scriptName, final String parameterType, final String parameterName) {
		listEventListeners.forEach(listener -> listener.readUnsupportedParameter(scriptName, parameterType, parameterName));
	}

	public void readSupportedFunction(final String scriptName, final String functionName) {
		listEventListeners.forEach(listener -> listener.readSupportedFunction(scriptName, functionName, 0));
	}

	public void readUnsupportedFunction(final String scriptName, final String functionName) {
		listEventListeners.forEach(listener -> listener.readUnsupportedFunction(scriptName, functionName, 1));
	}

	public void readSupportedFunctionWithWarn(final String scriptName, final String functionName, final String warning) {
		listEventListeners.forEach(listener -> listener.readSupportedFunctionWithWarn(scriptName, functionName, 0, warning));
	}

	public void readUnsupportedAction(String s) {
		listEventListeners.forEach(listener -> listener.readUnsupportedAction(s));
	}

	void endReadingScripts() {
		listEventListeners.forEach(EventListener::endReadingScripts);
	}

	void startReadingScripts(final int ligne) {
		listEventListeners.forEach(listener -> listener.startReadingScripts(ligne));
	}

	void endScript() {
		listEventListeners.forEach(EventListener::endScript);
	}

	void startScript(String name) {
		listEventListeners.forEach(listener -> listener.startScript(name));
	}
}