package com.example.skim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Word {

    public String text;
    public int x;
    public int y;
    public int width;
    public int height;

    public Word(String text, String coords) {
        this.text = text;

        Pattern pattern = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+)");
        Matcher matcher = pattern.matcher(coords);
        matcher.find();

        this.x = Integer.parseInt(matcher.group(1));
        this.y = Integer.parseInt(matcher.group(2));
        this.width = Integer.parseInt(matcher.group(3));
        this.height = Integer.parseInt(matcher.group(4));
    }
}
