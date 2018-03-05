package com.leokongwq.npj.reactor.singlethread;

import java.io.IOException;

/**
 * @author : kongwenqiang
 * DateTime: 2018/2/28 下午3:25
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class ServerBootstrap {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        Reactor reactor = new Reactor(PORT);
        new Thread(reactor).start();
    }
}
