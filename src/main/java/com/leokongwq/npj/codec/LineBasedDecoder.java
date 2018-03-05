package com.leokongwq.npj.codec;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author : kongwenqiang
 * DateTime: 2018/3/4 下午2:11
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class LineBasedDecoder {

    public static List<String> decode(ByteBuffer input) {
        List<String> frames = new LinkedList<>();
        input.flip();
        if (!input.hasRemaining()) {
            return Collections.emptyList();
        }
        int start = input.position();
        int end = start;
        while (end < input.limit()) {
            if (input.get(end) == '\n') {
                int frameLen = end - start;
                if (frameLen > 0) {
                    byte[] line = new byte[frameLen];
                    int j = 0;
                    for (int i = start; i < end; i++) {
                        line[j++] = input.get(i);
                    }
                    frames.add(new String(line));
                }
                start = end + 1;
            }
            end++;
        }
        if (start <= input.limit()) {
            input.position(start);
            input.compact();
        }
        return frames;
    }

    public static void main(String[] args) {
        try {
            ByteBuffer input = ByteBuffer.allocate(1024);
            input.put("\n\nhello\n\nworld\njava\n".getBytes());

            LineBasedDecoder decoder = new LineBasedDecoder();
            List<String> frames = decoder.decode(input);

            for (String frame : frames) {
                System.out.println(frame);
            }
            frames.clear();
            input.put("GET /index HTTP/1.1\n".getBytes());
            frames = decoder.decode(input);
            for (String frame : frames) {
                System.out.println(frame);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
