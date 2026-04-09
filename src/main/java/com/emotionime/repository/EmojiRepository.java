package com.emotionime.repository;

import java.util.*;

public class EmojiRepository {

    private static final Map<String, List<String>> map = new HashMap<>();

    static {
        // 开心 - 使用稳定的颜文字（删除不完整和复杂字符）
        map.put("happy", Arrays.asList(
            "(^_^)", "(^o^)", "(*^__^*)", 
            "(≧▽≦)", "(◕‿◕)", "(✿◕‿◕✿)",
            "(´▽`ʃ♡ƪ)", "(ﾉ◕ヮ◕)ﾉ*:・ﾟ✧", "(●'◡'●)ﾉ♥"
        ));
        
        // 难过/伤心 - 删除复杂组合字符
        map.put("sad", Arrays.asList(
            "(T_T)", "(；＿；)", "(┬﹏┬)", "(╥_╥)", "(⊙︿⊙)",
            "(；′⌒`)", "(╥╯^╰╥)", "(ಥ_ಥ)", "(︶︹︺)",
            "(；ω；)", "(┬_┬)", "(╯︵╰)"
        ));
        
        // 生气 - 删除复杂组合字符如 Ĺ̯ ̿ ꐦ°᷄д°᷅
        map.put("angry", Arrays.asList(
            "(╬ಠ益ಠ)", "(¬_¬)", "(╬◣д◢)",
            "(◉_◉)", "(╯°□°)╯ ┻━┻"
        ));
        
        // 惊讶/震惊 - 删除复杂组合
        map.put("surprised", Arrays.asList(
            "(°o°)", "(⊙_)", "(⊙o⊙)", "(ﾟoﾟ;", 
            "(◎_◎)", "(⊙_◎)", "(⊙x)"
        ));
        
        // 害羞/尴尬 - 删除不完整颜文字
        map.put("shy", Arrays.asList(
            "(*/ω＼*)", "(〃ω〃)", "(◕‿◕)", "(◕◕)", "(◕‿◕)"
        ));
        
        // 困倦/疲惫
        map.put("tired", Arrays.asList(
            "(～﹃～)", "(∪｡∪)", "(⊙_;)", "(´-ω-`)", "(˘ω˘)"
        ));
        
        // 疑问/思考
        map.put("thinking", Arrays.asList(
            "(・_・;)", "(◕_◕)", "(⊙.⊙)", "(⊙_⊙;)"
        ));
        
        // 中立/默认
        map.put("neutral", Arrays.asList(
            "(-_-)", "(◕‿◕)", "(⊙.☉)", "(◕_◕)"
        ));
    }

    public List<String> getByEmotion(String emotion) {
        return map.getOrDefault(emotion, map.get("neutral"));
    }
}
