package com.leokongwq.npj.reactor.singlethread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author : kongwenqiang
 * DateTime: 2018/2/28 下午3:37
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class ChannelHandler implements Runnable {

    private final SelectionKey selectionKey;
    private final SocketChannel clientChannel;

    private ByteBuffer input = ByteBuffer.allocate(4096);
    private ByteBuffer output = ByteBuffer.allocate(4096);

    private static final int READING = 0, SENDING = 1;

    private int state = READING;

    ChannelHandler(SelectionKey selectionKey, SocketChannel clientChannel) {
        this.selectionKey = selectionKey;
        this.clientChannel = clientChannel;
    }

    private boolean inputIsComplete() {
        return true;
    }

    private boolean outputIsComplete() {
        return true;
    }

    private void process() {
        System.out.println(new String(input.array()));
        output.put("response".getBytes());
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void read() throws IOException {
        clientChannel.read(input);
        if (inputIsComplete()) {
            process();
            state = SENDING;
            // Normally also do first write now
            //第三步,接收write事件
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void send() throws IOException {
        clientChannel.write(output);
        //write完就结束了, 关闭select key
        if (outputIsComplete()) {
            selectionKey.cancel();
        }
    }
}
