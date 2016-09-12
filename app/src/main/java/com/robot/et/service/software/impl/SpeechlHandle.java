package com.robot.et.service.software.impl;

import com.robot.et.service.software.SpeechRecognizer;
import com.robot.et.service.software.SpeechSynthesizer;
import com.robot.et.service.software.TextUnderstander;
import com.robot.et.service.software.turing.TuringUnderstander;

/**
 * Created by houdeming on 2016/7/25.
 */
public class SpeechlHandle {
    private static SpeechRecognizer speechRecognizer;
    private static SpeechSynthesizer speechSynthesizer;
    private static TextUnderstander textUnderstander;
    private static TuringUnderstander turingUnderstander;

    public static void setSpeechRecognizer(SpeechRecognizer speechRecognizer) {
        SpeechlHandle.speechRecognizer = speechRecognizer;
    }

    public static void setSpeechSynthesizer(SpeechSynthesizer speechSynthesizer) {
        SpeechlHandle.speechSynthesizer = speechSynthesizer;
    }

    public static void setTextUnderstander(TextUnderstander textUnderstander) {
        SpeechlHandle.textUnderstander = textUnderstander;
    }

    public static void setTuringUnderstander(TuringUnderstander turingUnderstander) {
        SpeechlHandle.turingUnderstander = turingUnderstander;
    }

    public static void startSpeak(int speakType, String speakContent) {
        if (speechSynthesizer != null) {
            speechSynthesizer.startSpeak(speakType, speakContent);
        }
    }

    public static void cancelSpeak() {
        if (speechSynthesizer != null) {
            speechSynthesizer.cancelSpeak();
        }
    }

    public static void startListen() {
        if (speechRecognizer != null) {
            speechRecognizer.startListen();
        }
    }

    public static void cancelListen() {
        if (speechRecognizer != null) {
            speechRecognizer.cancelListen();
        }
    }

    public static void understanderText (String content) {
        if (textUnderstander != null) {
            textUnderstander.understanderText(content);
        }
    }

    public static void turingUnderstander (String content) {
        if (turingUnderstander != null) {
            turingUnderstander.understanderText(content);
        }
    }

}
