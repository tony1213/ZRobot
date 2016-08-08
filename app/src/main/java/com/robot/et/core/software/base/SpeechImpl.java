package com.robot.et.core.software.base;

/**
 * Created by houdeming on 2016/8/8.
 */
public class SpeechImpl implements Speech {
    private static BaseService service;
    private static SpeechImpl instance = null;

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
        BaseService service = getService();
        if (service != null) {
            service.startSpeak(speakType, speakContent);
        }
    }

    @Override
    public void cancelSpeak() {
        BaseService service = getService();
        if (service != null) {
            service.cancelSpeak();
        }
    }

    @Override
    public void startListen() {
        BaseService service = getService();
        if (service != null) {
            service.startListen();
        }
    }

    @Override
    public void cancelListen() {
        BaseService service = getService();
        if (service != null) {
            service.cancelListen();
        }
    }

    @Override
    public void understanderTextByIfly(String content) {
        BaseService service = getService();
        if (service != null) {
            service.understanderTextByIfly(content);
        }
    }

    @Override
    public void understanderTextByTuring(String content) {
        BaseService service = getService();
        if (service != null) {
            service.understanderTextByTuring(content);
        }
    }

    public static void setService(BaseService service) {
        SpeechImpl.service = service;
    }

    public static BaseService getService() {
        return service;
    }

}
