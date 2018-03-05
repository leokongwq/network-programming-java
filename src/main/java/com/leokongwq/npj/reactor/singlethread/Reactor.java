package com.leokongwq.npj.reactor.singlethread;

import com.leokongwq.npj.codec.LineBasedDecoder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : kongwenqiang
 * DateTime: 2018/2/28 下午3:25
 * Mail:leokongwq@gmail.com   
 * Description: desc
 */
public class Reactor implements Runnable {

    private int MAXIN = 4096;

    private int MAXOUT = 4096;

    private final Selector selector;

    private final ServerSocketChannel serverSocket;

    private static ExecutorService pool = Executors.newFixedThreadPool(5);

    Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        //attach callback object, Acceptor
        sk.attach(new Acceptor());
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

    class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    new Handler(selector, c);
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    final class Handler implements Runnable {

        final SocketChannel socket;
        final SelectionKey sk;
        ByteBuffer input = ByteBuffer.allocate(MAXIN);
        ByteBuffer output = null;

        Handler(Selector sel, SocketChannel c) throws IOException {
            socket = c;
            c.configureBlocking(false);
            // Optionally try first read now
            sk = socket.register(sel, 0);
            socket.socket().setTcpNoDelay(true);

            sk.attach(this);
            sk.interestOps(SelectionKey.OP_READ);
            sel.wakeup();
        }

        @Override
        public void run() {
            try {
                if (sk.isReadable()) {
                    read();
                }
                else if (sk.isWritable()) {
                    send();
                }
            } catch (IOException ex) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }

        boolean inputIsComplete() {
            return true;
        }

        boolean outputIsComplete() {
            return true;
        }

        void process(String msg) {
            System.out.println(msg);
            output = ByteBuffer.wrap(("I'am server " + System.currentTimeMillis()).getBytes());
        }

        synchronized void read() throws IOException {
            int readCnt = socket.read(input);
            if (readCnt == 0) {
                return;
            }
            List<String> msgList = LineBasedDecoder.decode(input);

            if (!msgList.isEmpty()) {
                //process(readCnt);
                //使用线程pool异步执行
                for (String msg : msgList) {
                    pool.execute(new Processor(msg));
                }
            }
        }

        private List<String> decode(ByteBuffer input) {
            if (input.position() == 0) {
                return Collections.emptyList();
            }
            input.flip();

            List<String> msgList = new ArrayList<>();
            int i = 0;

            while (i < input.limit()) {
                if (input.get(i) != '\n') {
                    i++;
                } else {
                    msgList.add(new String(input.array(), 0, i));
                    input.compact();
                    i = 0;
                }
            }
            return msgList;
        }

        void send() throws IOException {
            if (output != null) {
                socket.write(output);
                if (outputIsComplete()) {
                    output = null;
                }
                sk.interestOps(sk.interestOps() & ~SelectionKey.OP_WRITE);
            }
        }

        synchronized void processAndHandOff(String msg) {
            process(msg);
            //注册写事件
            sk.interestOps(SelectionKey.OP_WRITE);
        }

        class Processor implements Runnable {

            private String msg;

            Processor(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                processAndHandOff(msg);
            }
        }
    }
}
