package com.leokongwq.npj.reactor.singlethread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

/**
 * @author : kongwenqiang
 * DateTime: 2018/2/28 下午3:25
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class Reactor implements Runnable {

    private final Selector selector;

    Reactor(int port) throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        //attach callback object, Acceptor
        sk.attach(new Acceptor(selector, serverSocket));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                for (SelectionKey selectionKey : selected) {
                    //Reactor负责dispatch收到的事件
                    dispatch(selectionKey);
                }
                selected.clear();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        //调用之前注册的callback对象
        Runnable r = (Runnable)(selectionKey.attachment());
        if (r != null) {
            r.run();
        }
    }
}
