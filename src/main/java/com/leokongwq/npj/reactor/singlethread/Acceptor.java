package com.leokongwq.npj.reactor.singlethread;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author : kongwenqiang
 * DateTime: 2018/2/28 下午3:30
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class Acceptor implements Runnable {

    private Selector selector;
    private ServerSocketChannel serverSocket;

    Acceptor(Selector selector, ServerSocketChannel serverSocket) {
        this.selector = selector;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            SocketChannel clientChannel = serverSocket.accept();
            clientChannel.configureBlocking(false);
            SelectionKey sk = clientChannel.register(selector, 0);
            //将Handler作为callback对象
            sk.attach(new ChannelHandler(sk, clientChannel));
            //第二步,接收Read事件
            sk.interestOps(SelectionKey.OP_READ);
            selector.wakeup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
