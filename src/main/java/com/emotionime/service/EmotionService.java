package com.emotionime.service;

import java.util.Locale;

public class EmotionService {

    public String detect(String text) {
        if (text == null || text.isEmpty()) return "neutral";

        String value = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");

        // 开心类
        if (anyMatch(value,
                // 中文
                "开心", "高兴", "快乐", "愉快", "幸福", "满意", "爽", "棒", "好棒",
                "哈哈", "嘻嘻", "嘿嘿", "笑", "大笑", "微笑",
                "太棒了", "好开心", "开心死了", "爽死了", "美滋滋", "乐呵呵",
                "nice", "good", "great", "wonderful", "perfect", "amazing",
                "happy", "glad", "joy", "fun", "awesome", "excited", "love",
                // 拼音
                "kaixin", "gaoxing", "kuaile", "yukuai", "xingfu", "shuang",
                "haha", "xixi", "xiao", "bang", "haobang", "lehehe", "meizizi",
                "haokuaile", "taibangle")) {
            return "happy";
        }

        // 难过/伤心类
        if (anyMatch(value,
                "难过", "伤心", "悲伤", "悲痛", "悲哀", "心痛", "心酸",
                "哭", "哭了", "想哭", "大哭", "痛哭", "流泪", "眼泪",
                "难受", "委屈", "失望", "绝望", "可怜", "心疼",
                "伤心死了", "难过死了", "心碎", "崩溃", "emo",
                "sad", "cry", "unhappy", "depressed", "upset", "heartbroken",
                "nanguo", "shangxin", "beishang", "ku", "nanshou", "weiqu",
                "shiwang", "kelian", "xintong", "juewang", "bengkui",
                "xinsui", "liulei", "xiangku")) {
            return "sad";
        }

        // 生气类
        if (anyMatch(value,
                "生气", "愤怒", "恼火", "火大", "暴躁", "抓狂",
                "气死", "气死了", "气死我了", "烦", "烦死了", "烦死了",
                "讨厌", "讨厌死了", "可恶", "该死", "混蛋", "王八蛋",
                "怒", "发怒", "暴怒", "怒了", "炸了", "裂开",
                "angry", "mad", "furious", "annoyed", "hate", "pissed",
                "shengqi", "fennu", "naohuo", "huoda", "baozao", "zhuakuang",
                "qisi", "fan", "fansile", "taoyan", "kewu", "gaisi",
                "nu", "nuole", "zhale", "liekai")) {
            return "angry";
        }

        // 惊讶/震惊类
        if (anyMatch(value,
                "惊讶", "震惊", "吃惊", "诧异", "惊呆",
                "哇", "哇塞", "哇哦", "我去", "我靠", "我擦", "卧槽", "我天",
                "天哪", "天啊", "我的天", "我的天哪", "老天",
                "真的吗", "不会吧", "不可能", "什么鬼", "啥情况",
                "厉害", "牛", "牛逼", "nb", "666", "绝了",
                "omg", "oh my god", "oh my", "wow", "what", "seriously",
                "surprised", "shocked", "amazing", "unbelievable",
                "jingya", "zhenjing", "chijing", "jingdai",
                "wa", "wasa", "wao", "woqu", "wokao", "wocao", "wotian",
                "tianna", "tiana", "lihai", "niu", "niubi", "juele")) {
            return "surprised";
        }

        // 害羞/尴尬类
        if (anyMatch(value,
                "害羞", "羞", "脸红", "羞耻", "害臊",
                "尴尬", "社死", "社恐", "无地自容",
                "不好意思", "抱歉", "对不起", "sorry",
                "shy", "awkward", "embarrassed", "blush",
                "haixiu", "ganga", "lianhong", "xiuchi", "haisao",
                "shesi", "shekong", "buhaoyisi", "baoqian", "duibuqi")) {
            return "shy";
        }

        // 困倦/疲惫类
        if (anyMatch(value,
                "困", "困了", "困死了", "好困", "太困了", "犯困",
                "累", "累了", "累死了", "好累", "太累了", "疲惫", "疲倦",
                "想睡", "想睡觉", "睡觉", "睡了", "晚安",
                "没精神", "没力气", "无力", "乏力", "虚脱",
                "tired", "sleepy", "exhausted", "exhausting", "yawn",
                "kun", "kunsile", "haokun", "le", "leisile", "haolei",
                "tailele", "pibei", "pijuan", "xiangshui", "shuijiao",
                "wanan", "meijingshen", "meiliqi", "wuli", "fali")) {
            return "tired";
        }

        // 疑问/思考类
        if (anyMatch(value,
                "？", "?", "？？", "???",
                "什么", "啥", "什么鬼", "什么情况", "怎么回事",
                "为什么", "为啥", "为何", "咋回事", "咋了",
                "怎么", "怎么办", "怎么搞", "如何",
                "哪里", "哪儿", "哪个", "谁",
                "真的吗", "是吗", "对吗", "行吗", "能吗",
                "奇怪", "纳闷", "好奇", "疑惑", "不解",
                "thinking", "why", "what", "how", "where", "who", "really",
                "shenme", "weishenme", "zenme", "zahuishi", "zale",
                "nali", "nar", "nage", "shui",
                "zhendema", "shima", "duima", "xingma",
                "qiguai", "namen", "haoqi", "yihuo", "bujie")) {
            return "thinking";
        }

        return "neutral";
    }

    private boolean anyMatch(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
