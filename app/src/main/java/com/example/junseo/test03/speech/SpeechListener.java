package com.example.junseo.test03.speech;

import java.util.ArrayList;

/**
 * Interface for listening speeches.
 *
 * Implements this interface to be notified speeches that recognized from SpeechRecognizer.
 * This is especially needed for implementing Decorator Pattern, which adds new feature in
 * speech listener.
 */
public interface SpeechListener {
    // called on finish of recognizing with the list of recognized strings.
    void onSpeechRecognized(ArrayList<String> recognitions);
}
