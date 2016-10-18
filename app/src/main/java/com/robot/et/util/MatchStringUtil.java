package com.robot.et.util;

import android.text.TextUtils;

//字符串匹配
public class MatchStringUtil {

    private static String baseRegex = "[\u4E00-\u9FA5A-Za-z0-9_]";
    // 间接增加声音
    public static String voiceBiggerIndirectRegex = "^" + baseRegex + "*((声音)|(音量))+" + baseRegex + "*(小|低)+" + baseRegex + "*(大|高|增|升|响|强)+" + baseRegex + "*$";
    // 直接增加声音
    public static String voiceBiggerRegex = "^" + baseRegex + "*((((声音)|(音量))+" + baseRegex + "*(大|高)+)|((再大)|(再高)|(再增)|(再升))+)" + baseRegex + "*$";
    // 声音最大
    public static String voiceBigestRegex = "^" + baseRegex + "*((声音)|(音量))+" + baseRegex + "*((最大)|(最高))+" + baseRegex + "*$";
    // 间接降低声音
    public static String voiceLitterIndirectRegex = "^" + baseRegex + "*((声音)|(音量))+" + baseRegex + "*(大|高)+" + baseRegex + "*(小|低|减|降|弱)+" + baseRegex + "*$";
    // 直接降低声音
    public static String voiceLitterRegex = "^" + baseRegex + "*((((声音)|(音量))+" + baseRegex + "*(小|低)+)|((再小)|(再低)|(再减)|(再降))+)" + baseRegex + "*$";
    // 声音最小
    public static String voiceLittestRegex = "^" + baseRegex + "*((声音)|(音量))+" + baseRegex + "*((最小)|(最低))+" + baseRegex + "*$";

    //学习的问题与答案
    public static String questionAndAnswerRegex = "^" + baseRegex + "*我+" + baseRegex + "*((问)|(说))+" + baseRegex + "*你+" + baseRegex + "+((说)|(回答))+" + baseRegex + "+$";
    //免打扰开
    public static String disturbOpenRegex = "^" + baseRegex + "*(((免打扰)+" + baseRegex + "*开+)|(开+" + baseRegex + "*(免打扰)+))" + baseRegex + "*$";
    //免打扰关
    public static String disturbCloseRegex = "^" + baseRegex + "*(((免打扰)+" + baseRegex + "*关+)|(关+" + baseRegex + "*(免打扰)+))" + baseRegex + "*$";
    //闭嘴
    public static String shutUpRegex = "^" + baseRegex + "*((嘴+" + baseRegex + "*闭+)|(闭+" + baseRegex + "*嘴+)|((休息)|(睡觉)|(静音)|(别说话)+))" + baseRegex + "*$";
    //做动作
    public static String doActionRegex = "^" + baseRegex + "*我+" + baseRegex + "*((问)|(说))+" + baseRegex + "*你+" + baseRegex + "+(萌|(卖个萌))+" + baseRegex + "*$";
    //控制机器人周围的玩具车走
    public static String controlToyCarRegex = "^" + baseRegex + "+号+" + baseRegex + "*(车|(小车)|(玩具车)|(汽车))+" + baseRegex + "*$";
    // 抬左手
    public static String raiseLeftHandRegex = "^" + baseRegex + "*(抬|举)+" + baseRegex + "*(左手)+" + baseRegex + "*$";
    // 抬右手
    public static String raiseRightHandRegex = "^" + baseRegex + "*(抬|举)+" + baseRegex + "*(右手)+" + baseRegex + "*$";
    // 抬手举手
    public static String raiseHandRegex = "^" + baseRegex + "*(抬|举)+" + baseRegex + "*手+" + baseRegex + "*$";
    // 抬头
    public static String raiseHeadUpRegex = "^" + baseRegex + "*(抬|举|仰)+" + baseRegex + "*头+" + baseRegex + "*$";
    // 低头
    public static String raiseHeadDownRegex = "^" + baseRegex + "*(低|底)+" + baseRegex + "*头+" + baseRegex + "*$";
    // 摆手
    public static String wavingRegex = "^" + baseRegex + "*摆+" + baseRegex + "*手+" + baseRegex + "*$";
    // 表演节目
    public static String playScriptRegex = "^" + baseRegex + "*(表演)+" + baseRegex + "*((节目)|(剧本))+" + baseRegex + "*$";
    // 跳舞
    public static String danceRegex = "^" + baseRegex + "*跳+" + baseRegex + "*舞+" + baseRegex + "*$";
    // 脸检测
    public static String faceTestRegex = "^" + baseRegex + "*((我是谁)|(认识我))+" + baseRegex + "*$";
    // 识别问名字
    public static String faceNameRegex = "^" + baseRegex + "*我+" + baseRegex + "*(叫|是)+" + baseRegex + "+$";
    // 拍照
    public static String photographRegex = "^" + baseRegex + "*((拍+" + baseRegex + "*照+)|(照+" + baseRegex + "*相+))" + baseRegex + "*$";

    // 认识环境学习
    public static String environmentLearnRegex = "^" + baseRegex + "*这+" + baseRegex + "*(里|(地方))+" + baseRegex + "*是+" + baseRegex + "+$";
    // 认识物体
    public static String visionLearnRegex = "^" + baseRegex + "*这+" + baseRegex + "*是+" + baseRegex + "+$";
    // 去哪里
    public static String goWhereRegex = "^" + baseRegex + "*去+" + baseRegex + "+$";
    //忘记学习内容
    public static String forgetLearnRegex = "^" + baseRegex + "*(忘|删)+" + baseRegex + "*(学习)+" + baseRegex + "*$";
    //导航到
    public static String navigationRegex = "^" + baseRegex + "*(导航)+" + baseRegex + "*(到|去)+" + baseRegex + "+$";

    // 打开运动
    public static String openMotionRegex = "^" + baseRegex + "*开+" + baseRegex + "*(运动)+" + baseRegex + "*$";
    // 关闭运动
    public static String closeMotionRegex = "^" + baseRegex + "*关+" + baseRegex + "*(运动)+" + baseRegex + "*$";

    // 看看照片的标志
    public static String lookPhotoRegex = "^" + baseRegex + "*(看|(浏览))+" + baseRegex + "*((照片)|(图片)|(相册)|(相片))+" + baseRegex + "*$";
    // 上一张照片
    public static String lastPhotoRegex = "^" + baseRegex + "*((上一张)|(上一个)|(上一章))+" + baseRegex + "*$";
    // 下一张照片
    public static String nextPhotoRegex = "^" + baseRegex + "*((下一张)|(下一个)|(下一章))+" + baseRegex + "*$";

    // 进入安保场景的标志
    public static String openSecuritySignRegex = "^" + baseRegex + "*((进入)|(打开)|(开启))+" + baseRegex + "*((安保)|(巡防))+" + baseRegex + "*$";
    // 解除安保场景的标志
    public static String closeSecuritySignRegex = "^" + baseRegex + "*((解除)|(退出)|(关闭))+" + baseRegex + "*((安保)|(巡防))+" + baseRegex + "*$";

    // 漫游的标志
    public static String roamSignRegex = "^" + baseRegex + "*(((漫游)+)|(((自己)|随)+" + baseRegex + "*走+))" + baseRegex + "*$";
    // 跟着我的标志
    public static String followSignRegex = "^" + baseRegex + "*跟+" + baseRegex + "*我+" + baseRegex + "*$";

    // 机器人编号的标志
    public static String robotNumSignRegex = "^" + baseRegex + "*你+" + baseRegex + "*(编号)+" + baseRegex + "+$";
    // 讲故事
    public static String storyRegex = "^" + baseRegex + "*(讲|将|说)+" + baseRegex + "*(故事)+" + baseRegex + "*$";
    // 播放宣传片
    public static String playTrailerRegex = "^" + baseRegex + "*((播放)|(演示)|(展示))+" + baseRegex + "*((宣传)|(动画))+" + baseRegex + "*$";

    //匹配场景字符串
    public static boolean matchString(String str, String strRegex) {
        return str.matches(strRegex);
    }

    /**
     * @return String
     * @method getAnswerStr(获取答案)
     * @author ZHY
     * @date 2016年6月13日 下午8:00:11
     */
    public static String getAnswer(String questAndAnser) {
        int beginIndex = 0;
        int endIndex = 0;
        char[] str = questAndAnser.toCharArray();
        endIndex = str.length;
        for (int i = 0; i < str.length; i++) {
            if ((str[i] + "").equals("说")) {
                beginIndex = i + 1;
            }
            if ((str[i] + "").equals("回") && (str[i + 1] + "").equals("答")) {
                beginIndex = i + 2;
            }
        }
        return questAndAnser.substring(beginIndex, endIndex);
    }

    /**
     * @return String
     * @method getQuestionStr(获取问题)
     * @author ZHY
     * @date 2016年6月13日 下午8:00:06
     */
    public static String getQuestion(String questAndAnser) {
        int beginIndex = 0;
        int endIndex = 0;
        char[] str = questAndAnser.toCharArray();
        for (int i = 0; i < str.length; i++) {
            if ((str[i] + "").equals("说") || ((str[i] + "").equals("问"))) {
                beginIndex = i;
                break;
            }
        }
        for (int i = 0; i < str.length; i++) {
            if ((str[i] + "").equals("你")) {
                endIndex = i;
            }
        }
        String tempResult = questAndAnser.substring(++beginIndex, endIndex);
        char[] datas = tempResult.toCharArray();
        if (datas != null && datas.length > 0) {
            if (TextUtils.equals((datas[0] + ""), "你") && TextUtils.equals((datas[1] + ""), "你")) {
                tempResult = tempResult.substring(1);
            }
        }
        return tempResult;
    }

    //获取控制机器人周围小车的号码
    public static int getToyCarNum(String result) {
        int carNum = 0;
        if (!TextUtils.isEmpty(result)) {
            String sign = "号";
            if (result.contains(sign)) {
                int end = result.indexOf(sign);
                String data = result.substring(0, end);
                StringBuffer buffer = new StringBuffer();
                if (!TextUtils.isEmpty(data)) {
                    for (int i = 0; i < data.length(); i++) {
                        char tempChar = data.charAt(i);
                        if (Character.isDigit(tempChar)) {
                            buffer.append(tempChar);
                        } else if (TextUtils.equals(tempChar + "", "一")) {
                            buffer.append("1");
                        }
                    }
                    String num = buffer.toString();
                    if (!TextUtils.isEmpty(num)) {
                        if (TextUtils.isDigitsOnly(num)) {
                            carNum = Integer.parseInt(num);
                        }
                    }
                }
            }
        }
        return carNum;
    }

    // 获取人脸识别时候的名字
    public static String getFaceName(String str) {
        String content = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("是")) {
                int lastIndes = str.lastIndexOf("是");
                content = str.substring(lastIndes + 1, str.length());
            } else if (str.contains("叫")) {
                int lastIndes = str.lastIndexOf("叫");
                content = str.substring(lastIndes + 1, str.length());
            }
        }
        return content;
    }

    //获取视觉学习的答案
    public static String getVisionLearnAnswer(String str) {
        String content = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("什么") || str.contains("啥")) {
                content = "";
            } else {
                if (str.contains("是")) {
                    int start = str.indexOf("是");
                    content = str.substring(start + 1, str.length());
                }
            }
        }
        return content;
    }

    //获取环境学习的答案
    public static String getEnvironmentLearnAnswer(String str) {
        String content = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("是")) {
                int start = str.indexOf("是");
                content = str.substring(start + 1, str.length());
            }
        }
        return content;
    }

    //导航到
    public static String getNavigationArea(String str) {
        String content = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("到")) {
                int start = str.indexOf("到");
                content = str.substring(start + 1, str.length());
            } else if (str.contains("去")) {
                int start = str.indexOf("去");
                content = str.substring(start + 1, str.length());
            }
        }
        return content;
    }

    //获取到哪里的指令
    public static String getGoWhereAnswer(String str) {
        String content = "";
        if (!TextUtils.isEmpty(str)) {
            if (str.contains("去")) {
                if (str.contains("哪") || str.contains("那")) {
                    content = "";
                } else {
                    int start = str.indexOf("去");
                    content = str.substring(start + 1, str.length());
                }
            }
        }
        return content;
    }

}
