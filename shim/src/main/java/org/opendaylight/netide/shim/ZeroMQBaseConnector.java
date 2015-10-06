/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import io.netty.buffer.Unpooled;
import org.opendaylight.netide.netiplib.HelloMessage;
import org.opendaylight.netide.netiplib.Message;
import org.opendaylight.netide.netiplib.NetIPConverter;
import org.opendaylight.netide.netiplib.OpenFlowMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

public class ZeroMQBaseConnector implements Runnable {

    private static final String STOP_COMMAND = "Control.STOP";
    private static final String CONTROL_ADDRESS = "inproc://ShimControllerQueue";

    private static final Logger LOG = LoggerFactory.getLogger(ZeroMQBaseConnector.class);

    private int port;
    private ZMQ.Context context;
    private Thread thread;

    private ICoreListener coreListener;

    public ZeroMQBaseConnector() {

    }

    public void Start() {
        context = ZMQ.context(1);
        thread = new Thread(this);
        thread.setName("ZeroMQBasedConnector Receive Loop");
        thread.start();
    }

    public void Stop() {
        if (thread != null) {
            ZMQ.Socket stopSocket = context.socket(ZMQ.PUSH);
            stopSocket.connect(CONTROL_ADDRESS);
            stopSocket.send(STOP_COMMAND);
            stopSocket.close();
            try {
                thread.join();
                context.term();
            } catch (InterruptedException e) {
                LOG.error("", e);
            }
        }
        LOG.info("ZeroMQBasedConnector stopped.");
    }

    public void RegisterCoreListener(ICoreListener listener) {
        this.coreListener = listener;
    }

    public boolean SendData(byte[] data) {
        ZMsg msg = new ZMsg();
        msg.add("core");
        msg.add("");
        msg.add(data);
        // relayed via control socket to prevent threading issues
        ZMQ.Socket sendSocket = context.socket(ZMQ.PUSH);
        sendSocket.connect(CONTROL_ADDRESS);
        msg.send(sendSocket);
        sendSocket.close();
        return true;
    }

    @Override
    public void run() {
        LOG.info("ZeroMQBasedConnector started.");
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.setIdentity("shim".getBytes());
        socket.connect("tcp://localhost:" + port);
        LOG.info("Listening on port " + port);

        ZMQ.Socket controlSocket = context.socket(ZMQ.PULL);
        controlSocket.bind(CONTROL_ADDRESS);

        ZMQ.Poller poller = new ZMQ.Poller(2);
        poller.register(socket, ZMQ.Poller.POLLIN);
        poller.register(controlSocket, ZMQ.Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(10);
            if (poller.pollin(0)) {
                ZMsg message = ZMsg.recvMsg(socket);
                byte[] data = message.getLast().getData();
                if (coreListener != null) {
                    Message msg = NetIPConverter.parseConcreteMessage(data);
                    if (msg instanceof HelloMessage){
                        coreListener.onHelloCoreMessage(((HelloMessage)msg).getSupportedProtocols());
                    }else if (msg instanceof OpenFlowMessage){
                        coreListener.onOpenFlowCoreMessage(msg.getHeader().getDatapathId(), Unpooled.wrappedBuffer(msg.getPayload()));
                    }
                }
            }
            if (poller.pollin(1)) {
                ZMsg message = ZMsg.recvMsg(controlSocket);

                if (message.getFirst().toString().equals(STOP_COMMAND)) {
                    break;
                } else {
                    message.send(socket);
                }
            }
        }
        socket.close();
        controlSocket.close();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
