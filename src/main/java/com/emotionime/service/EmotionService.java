package com.emotionime.service;

import java.util.Locale;

public class EmotionService {

    public String detect(String text) {
        if (text == null || text.isEmpty()) return "neutral";
        
        String value = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");

        // 开心类
        if (value.contains("开心") || value.contains("happy") || value.contains("kaixin") ||
            value.contains("笑") || value.contains("哈哈") || value.contains("嘻嘻") ||
            value.contains("高兴") || value.contains("愉快") || value.contains("快乐") ||
            value.contains("joy") || value.contains("fun") || value.contains("awesome") ||
            value.contains("太棒了") || value.contains("棒") || value.contains("赞") ||
            value.contains("666") || value.contains("哈哈哈") || value.contains("嘿嘿") ||
            value.contains("好开心") || value.contains("开心") || value.contains("开心死我了")) {
            return "happy";
        }
        
        // 难过/伤心类
        if (value.contains("难过") || value.contains("伤心") || value.contains("sad") || value.contains("nanguo") ||
            value.contains("哭") || value.contains("呜呜") || value.contains("哭了") ||
            value.contains("伤心") || value.contains("悲伤") || value.contains("悲痛") ||
            value.contains("难过") || value.contains("难过") || value.contains("难过了") ||
            value.contains("哭了") || value.contains("想哭") || value.contains("难受") ||
            value.contains("伤心死了") || value.contains("伤心死了")) {
            return "sad";
        }
        
        // 生气类
        if (value.contains("生气") || value.contains("angry") || value.contains("shengqi") ||
            value.contains("愤怒") || value.contains("火大") || value.contains("气死") ||
            value.contains("生气") || value.contains("气死了") || value.contains("烦死了") ||
            value.contains("讨厌") || value.contains("讨厌死了") || value.contains("烦死了") ||
            value.contains("可恶") || value.contains("该死") || value.contains("混蛋") ||
            value.contains("生气死了") || value.contains("气死我了") || value.contains("生气死了")) {
            return "angry";
        }
        
        // 惊讶/震惊类
        if (value.contains("惊讶") || value.contains("震惊") || value.contains("surprised") || value.contains("shocked") ||
            value.contains("哇塞") || value.contains("哇哦") || value.contains("天哪") || value.contains("天啊") ||
            value.contains("my god") || value.contains("omg") || value.contains("oh my") || value.contains("oh my god") ||
            value.contains("我的天哪") || value.contains("我的天哪")) {
            return "surprised";
        }
        
        // 害羞/尴尬类
        if (value.contains("害羞") || value.contains("shy") || value.contains("haixiu") ||
            value.contains("尴尬") || value.contains("awkward") || value.contains("gan'ga") || value.contains("ga'n'ga") ||
            value.contains("不好意思") || value.contains("不好意思") || value.contains("害羞死了") ||
            value.contains("太尴尬了") || value.contains("太尴尬了")) {
            return "shy";
        }
        
        // 困倦/疲惫类
        if (value.contains("困") || value.contains("累") || value.contains("tired") || value.contains("lei") ||
            value.contains("sleepy") || value.contains("困死了") || value.contains("好困") ||
            value.contains("太累了") || value.contains("好累") || value.contains("累死了")) {
            return "tired";
        }
        
        // 疑问/思考类
        if (value.contains("？") || value.contains("?") || value.contains("thinking") || value.contains("思考") ||
            value.contains("什么") || value.contains("why") || value.contains("wei") || value.contains("怎么") ||
            value.contains("什么鬼") || value.contains("什么情况") || value.contains("为什么") ||
            value.contains("咋回事") || value.contains("怎么回事") || value.contains("咋回事") ||
            value.contains("什么鬼") || value.contains("什么情况")) {
            return "thinking";
        }
        
        return "neutral";
    }
}