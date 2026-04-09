package com.emotionime.service;

import com.emotionime.repository.EmojiRepository;
import java.util.*;

public class EmojiService {

    private final EmojiRepository repo = new EmojiRepository();
    private final Random random = new Random();

    public String getEmoji(String emotion) {
        List<String> list = repo.getByEmotion(emotion);
        return list.get(random.nextInt(list.size()));
    }

    public List<String> getEmojiList(String emotion) {
        List<String> original = repo.getByEmotion(emotion);

        List<String> list = new java.util.ArrayList<>(original);

        while (list.size() < 3) {
            list.add(list.get(0)); // 或 "(・_・)"
        }
        
        // 随机打乱前3个，确保每次都有变化
        Collections.shuffle(list);
        return list.subList(0, Math.min(3, list.size()));
    }
}