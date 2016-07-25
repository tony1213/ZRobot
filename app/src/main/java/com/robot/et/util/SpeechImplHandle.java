package com.robot.et.util;

import com.robot.et.impl.SpeechRecognizerImpl;
import com.robot.et.impl.SpeechSynthesizerImpl;

/**
 * Created by houdeming on 2016/7/25.
 */
public class SpeechImplHandle {
    private static SpeechRecognizerImpl speechRecognizer;
    private static SpeechSynthesizerImpl speechSynthesizer;

    public static void setSpeechRecognizer(SpeechRecognizerImpl speechRecognizer) {
        SpeechImplHandle.speechRecognizer = speechRecognizer;
    }

    public static void setSpeechSynthesizer(SpeechSynthesizerImpl speechSynthesizer) {
        SpeechImplHandle.speechSynthesizer = speechSynthesizer;
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

}
