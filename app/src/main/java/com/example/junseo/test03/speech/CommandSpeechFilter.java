package com.example.junseo.test03.speech;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Filters the speeches delivered by SpeechListener, using the designated speeches in advance.
 */
public class CommandSpeechFilter implements SpeechListener {
    private String TAG = CommandSpeechFilter.class.getSimpleName();
    private SpeechListener speech_listener_;
    private HashMap<String, String> pattern_speeches_ = new HashMap<>();

    private final String kVariantsDelemiter = "|";

    /**
     * Load patterns from setting file.
     * @param file_path file that contains speech patterns.
     * @param listener listener that will be notified after filtering.
     * @return new CommandSpeechFilter object
     */
    public static CommandSpeechFilter createFromFile(String file_path, SpeechListener listener) {
        // TODO: implement this.
        throw new UnsupportedOperationException();
    }

    public CommandSpeechFilter(SpeechListener listener) {
        speech_listener_ = listener;
    }

    /**
     * Add patterns that it is listening to.
     * @param speech the speech it is listening to.
     * @param variants similar speeches that are recognized as the speech, which is for increasing
     *                 recognition accuracy.
     */
    public void addPattern(String speech, ArrayList<String> variants) {
        // push variants
        for (String variant : variants) {
            String previous = pattern_speeches_.put(variant, speech);
            if (previous != null) {
                Log.w(TAG, "pattern duplicated: " + previous);
            }
        }
        // push itself.
        pattern_speeches_.put(speech, speech);
    }

    public void addPattern(String speech, String formatted_variants) {
        StringTokenizer tokenizer = new StringTokenizer(formatted_variants, kVariantsDelemiter);
        ArrayList<String> variants = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            variants.add(tokenizer.nextToken().trim());
        }
        addPattern(speech, variants);
    }

    @Override
    public void onSpeechRecognized(ArrayList<String> recognitions) {
        ArrayList<String> commands = new ArrayList<>();
        for (String speech : recognitions) {
            String pattern = pattern_speeches_.get(speech);

            if (pattern != null) {
                commands.add(pattern);
            } else {
                Log.d(TAG, "filtered: " + speech);
            }
        }
        speech_listener_.onSpeechRecognized(commands);
    }
}
