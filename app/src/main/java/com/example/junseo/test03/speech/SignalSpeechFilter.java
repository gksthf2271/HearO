package com.example.junseo.test03.speech;

import android.util.Log;

import java.util.ArrayList;

/**
 * Recognize the signal speech, which is a speech that voice command starts with.
 *
 * It delivers only the speeches that start with signal speech, to the next SpeechListener.
 */
public class SignalSpeechFilter implements SpeechListener {
    private String TAG = SignalSpeechFilter.class.getSimpleName();
    private SpeechListener speech_listener_;
    private String signal_speech_;

    /**
     * Create instance from setting file.
     * @param file_path setting file that contains signal speech.
     * @param speech_listener listener that will be notified after filtering.
     * @return new SignalSpeechFilter object.
     */
    public static SignalSpeechFilter createFromFile(String file_path,
                                                    SpeechListener speech_listener) {
        return null;
    }

    public SignalSpeechFilter(SpeechListener speech_listener, String signal_speech) {
        speech_listener_ = speech_listener;
        signal_speech_ = signal_speech;
        // add a space to make it easier to compare with speech string.
        signal_speech_ += " ";
    }

    /**
     * Set signal speech.
     * @param signal_speech new signal speech string.
     * @return old signal speech string.
     */
    public String setSignalSpeech(String signal_speech) {
        String old = signal_speech_;
        signal_speech_ = signal_speech;
        return old;
    }

    @Override
    public void onSpeechRecognized(ArrayList<String> recognitions) {
        ArrayList<String> result_without_signal = new ArrayList<>();
        for (String speech : recognitions) {
            if (speech.startsWith(signal_speech_)) {
                String signal_removed = speech.substring(signal_speech_.length());
                result_without_signal.add(signal_removed);
            } else {
                Log.d(TAG, "filtered: " + speech);
            }
        }

        // notify with signal string.
        speech_listener_.onSpeechRecognized(result_without_signal);
    }
}
