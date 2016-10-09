package com.robot.et.common.enums;

import com.robot.et.util.MatchStringUtil;

// 语音指令场景枚举类
public enum MatchSceneEnum {

    VOICE_BIGGEST_SCENE {// 声音最大

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceBigestRegex);
        }
    },
    VOICE_LITTEST_SCENE {// 声音最小

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceLittestRegex);
        }
    },
    VOICE_BIGGER_INDIRECT_SCENE {// 间接增加声音

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceBiggerIndirectRegex);
        }
    },
    VOICE_LITTER_INDIRECT_SCENE {// 间接降低声音

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceLitterIndirectRegex);
        }
    },
    VOICE_BIGGER_SCENE {// 直接增加声音

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceBiggerRegex);
        }
    },
    VOICE_LITTER_SCENE {// 直接降低声音

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.voiceLitterRegex);
        }
    },
    QUESTION_ANSWER_SCENE {// 智能学习回答话语

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.questionAndAnswerRegex);
        }
    },
    DISTURB_OPEN_SCENE {// 免打扰开

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.disturbOpenRegex);
        }
    },
    DISTURB_CLOSE_SCENE {// 免打扰关

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.disturbCloseRegex);
        }
    },
    SHUT_UP_SCENE {// 闭嘴

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.shutUpRegex);
        }
    },
    DO_ACTION_SCENE {// 智能学习做动作

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.doActionRegex);
        }
    },
    CONTROL_TOYCAR_SCENE {// 控制玩具车

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.controlToyCarRegex);
        }
    },
    RAISE_LEFT_HAND_SCENE {// 抬左手

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.raiseLeftHandRegex);
        }
    },
    RAISE_RIGHT_HAND_SCENE {// 抬右手

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.raiseRightHandRegex);
        }
    },
    RAISE_HAND_SCENE {// 抬手

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.raiseHandRegex);
        }
    },
    HEAD_UP_SCENE {// 抬头

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.raiseHeadUpRegex);
        }
    },
    HEAD_DOWN_SCENE {// 低头

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.raiseHeadDownRegex);
        }
    },
    WAVING_SCENE {// 摆手

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.wavingRegex);
        }
    },
    PLAY_SCRIPT_SCENE {// 表演节目

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.playScriptRegex);
        }
    },
    OPEN_MOTION_SCENE {// 打开运动

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.openMotionRegex);
        }
    },
    CLOSE_MOTION_SCENE {// 关闭运动

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.closeMotionRegex);
        }
    },
    FACE_TEST_SCENE {// 脸部检测

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.faceTestRegex);
        }
    },
    FACE_NAME_SCENE {// 脸部名称

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.faceNameRegex);
        }
    },
    PHOTOGRAPH_SCENE {// 拍照

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.photographRegex);
        }
    },
    ENVIRONMENT_LEARN_SCENE {// 认识环境学习

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.environmentLearnRegex);
        }
    },
    VISION_LEARN_SCENE {// 视觉学习

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.visionLearnRegex);
        }
    },
    GO_WHERE_SCENE {// 去哪里的指令

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.goWhereRegex);
        }
    },
    FORGET_LEARN_SCENE {// 忘记学习内容

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.forgetLearnRegex);
        }
    },
    NAVIGATION_SCENE {// 导航到

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.navigationRegex);
        }
    },
    ROAM_SCENE {// 漫游

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.roamSignRegex);
        }
    },
    FOLLOW_SCENE {// 跟着我

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.followSignRegex);
        }
    },
    LOOK_PHOTO_SCENE {// 看看照片的标志

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.lookPhotoRegex);
        }
    },
    ROBOT_NUM_SCENE {// 机器人编号的标志

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.robotNumSignRegex);
        }
    },
    STORY_SCENE {// 讲故事的标志

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.storyRegex);
        }
    },
    OPEN_SECURITY_SCENE {// 进入安保场景的标志

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.openSecuritySignRegex);
        }
    },
    CLOSE_SECURITY_SCENE {// 解除安保场景的标志

        public boolean isScene(String str) {
            return MatchStringUtil.matchString(str, MatchStringUtil.closeSecuritySignRegex);
        }
    };

    public abstract boolean isScene(String str);

}
