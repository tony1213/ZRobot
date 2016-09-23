package com.robot.et.core.software.common.speech;

import com.robot.et.core.software.voice.TextToVoiceService;
import com.robot.et.core.software.voice.TextUnderstanderService;
import com.robot.et.core.software.voice.VoiceToTextService;

/**
 * Created by houdeming on 2016/8/8.
 * 对外提供调用语音
 */
public class SpeechImpl implements Speech {
    private static SpeechImpl instance = null;
    private static TextToVoiceService speakService;
    private static VoiceToTextService voiceToTextService;
    private static TextUnderstanderService understanderService;

    private SpeechImpl() {
    }

    public static SpeechImpl getInstance() {
        if (instance == null) {
            synchronized (SpeechImpl.class) {
                if (instance == null) {
                    instance = new SpeechImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void startSpeak(int speakType, String speakContent) {
        if (speakService != null) {
            speakService.startSpeak(speakType, speakContent);
        }
    }

    @Override
    public void cancelSpeak() {
        if (speakService != null) {
            speakService.cancelSpeak();
        }
    }

    @Override
    public void startListen() {
        if (voiceToTextService != null) {
            voiceToTextService.startListen();
        }
    }

    @Override
    public void cancelListen() {
        if (voiceToTextService != null) {
            voiceToTextService.cancelListen();
        }
    }

    @Override
    public void understanderText(String content) {
        if (understanderService != null) {
            understanderService.understanderText(content);
        }
    }

    public static void setService(TextToVoiceService service) {
        SpeechImpl.speakService = service;
    }

    public static void setService(VoiceToTextService service) {
        SpeechImpl.voiceToTextService = service;
    }

    public static void setService(TextUnderstanderService service) {
        SpeechImpl.understanderService = service;
    }
}
