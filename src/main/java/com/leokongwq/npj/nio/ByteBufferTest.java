package com.leokongwq.npj.nio;

import java.nio.ByteBuffer;

/**
 * @author : kongwenqiang
 * DateTime: 2018/3/3 下午12:22
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class ByteBufferTest {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        buffer.put((byte)'H').put((byte)'e').put((byte)'l').put((byte)'l').put((byte)'o');

        //System.out.println(buffer.mark());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());

        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        buffer.flip();

        //System.out.println(buffer.mark());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.capacity());
    }

}
