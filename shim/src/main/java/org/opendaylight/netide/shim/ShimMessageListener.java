/*
 * Copyright (c) 2015 NetIDE Consortium and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.netide.shim;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.Future;
import org.opendaylight.netide.netiplib.Message;
import org.opendaylight.netide.netiplib.MessageType;
import org.opendaylight.netide.netiplib.NetIPUtils;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionReadyListener;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.serialization.factories.PacketInMessageFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OpenflowProtocolListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.DisconnectEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SwitchIdleEvent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.system.rev130927.SystemNotificationsListener;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShimMessageListener
        implements OpenflowProtocolListener, SystemNotificationsListener, ConnectionReadyListener {

    private static final Logger LOG = LoggerFactory.getLogger(ShimMessageListener.class);
    private static HashMap<ConnectionAdapter, BigInteger> connectionAdapterMap;
    public static final Long DEFAULT_XID = 0x01020304L;
    private ZeroMQBaseConnector coreConnector;
    private ConnectionAdapter switchConnection;

    public ShimMessageListener(ZeroMQBaseConnector connector, ConnectionAdapter switchConnection, HashMap<ConnectionAdapter, BigInteger> map) {
        this.coreConnector = connector;
        this.switchConnection = switchConnection;
        ShimMessageListener.connectionAdapterMap = map;
    }
    
    public static synchronized void setConnectionAdapterMap(HashMap<ConnectionAdapter, BigInteger> map){
        connectionAdapterMap = map;
    }
    
    public static synchronized void registerConnectionAdapter(ConnectionAdapter connectionAdapter, BigInteger datapathID){
        connectionAdapterMap.put(connectionAdapter, datapathID);
    }
    
    public static synchronized BigInteger getDatapathID(ConnectionAdapter connectionAdapter){
        return connectionAdapterMap.get(connectionAdapter);
    }
    
    private void sendToCore(byte[] data, long xId) {
        Message message = new Message(NetIPUtils.StubHeaderFromPayload(data), data);
        message.getHeader().setMessageType(MessageType.OPENFLOW);
        // TODO: find the correct values
        message.getHeader().setDatapathId(connectionAdapterMap.get(switchConnection).longValue());
        message.getHeader().setModuleId(0);
        message.getHeader().setTransactionId((int)xId);
        coreConnector.SendData(message.toByteRepresentation());
    }

    /// OpenflowProtocolListener methods/////
    @Override
    public void onEchoRequestMessage(EchoRequestMessage arg0) {
        LOG.info("SHIM Echo request message received: ", arg0);
        EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(arg0.getXid() + 1L);
        builder.setData(arg0.getData());
        this.switchConnection.echoReply(builder.build());
        if (getDatapathID(this.switchConnection) == null){
            LOG.info("SHIM Echo request message received: collect DatapathID");
            registerDatapathID((short)EncodeConstants.OF13_VERSION_ID, arg0.getXid() + 2L, this.switchConnection);
        }else{
            LOG.info("SHIM Echo request message received: DatapathID collected: " + getDatapathID(this.switchConnection).longValue());
        }
            
    }

    @Override
    public void onErrorMessage(ErrorMessage arg0) {
        LOG.info("SHIM Message received: ", arg0);
    }

    @Override
    public void onExperimenterMessage(ExperimenterMessage arg0) {
        LOG.info("SHIM Experimenter message received: ", arg0);
    }

    @Override
    public void onFlowRemovedMessage(FlowRemovedMessage arg0) {
        LOG.info("SHIM Flow removed message received: ", arg0);
    }

    @Override
    public void onHelloMessage(HelloMessage arg0) {
        LOG.info("SHIM Hello Message received: ", arg0);
        HelloInputBuilder builder = new HelloInputBuilder();
        builder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        builder.setXid(arg0.getXid() + 1L);
        builder.setElements(arg0.getElements());
        switchConnection.hello(builder.build());
    }

    @Override
    public void onMultipartReplyMessage(MultipartReplyMessage arg0) {
        LOG.info("SHIM Multipart reply message received: ", arg0);
    }

    @Override
    public void onPacketInMessage(PacketInMessage pIn) {
        LOG.info("SHIM Packet In message received: ", pIn);
        ByteBuf out = Unpooled.buffer();
        PacketInMessageFactory factory = new PacketInMessageFactory();
        factory.serialize(pIn, out);
        sendToCore(out.array(), pIn.getXid());
    }

    @Override
    public void onPortStatusMessage(PortStatusMessage arg0) {
        LOG.info("SHIM Port Status message received: ", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onDisconnectEvent(DisconnectEvent arg0) {
        LOG.info("SHIM Disconnect event received: ", arg0);
    }

    @Override
    public void onSwitchIdleEvent(SwitchIdleEvent arg0) {
        LOG.info("SHIM Switch Idle event received: ", arg0);
    }

    //// SystemNotificationsListener methods ////
    @Override
    public void onConnectionReady() {
        LOG.info("SHIM Message: ConnectionReady"); 
    }
    
    private void registerDatapathID(final Short proposedVersion, final Long xid, final ConnectionAdapter connectionAdapter) {
        LOG.debug("version set: {}", proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(proposedVersion).setXid(xid);
        LOG.debug("sending feature request for version={} and xid={}", proposedVersion, xid);
        Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                .getFeatures(featuresBuilder.build());

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(featuresFuture),
                new FutureCallback<RpcResult<GetFeaturesOutput>>() {
                    @Override
                    public void onSuccess(RpcResult<GetFeaturesOutput> rpcFeatures) {
                        LOG.trace("features are back");
                        if (rpcFeatures.isSuccessful()) {
                            GetFeaturesOutput featureOutput = rpcFeatures.getResult();

                            LOG.debug("obtained features: datapathId={}",
                                    featureOutput.getDatapathId());
                            registerConnectionAdapter(connectionAdapter, featureOutput.getDatapathId());
                        } else {
                            // handshake failed
                            LOG.warn("issuing disconnect during handshake [{}]", connectionAdapter.getRemoteAddress());
                            for (RpcError rpcError : rpcFeatures.getErrors()) {
                                LOG.debug("handshake - features failure [{}]: i:{} | m:{} | s:{}", xid,
                                        rpcError.getInfo(), rpcError.getMessage(), rpcError.getSeverity(),
                                        rpcError.getCause()
                                );
                            }
                        }

                        LOG.debug("postHandshake DONE");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.warn("getting feature failed seriously [{}, addr:{}]: {}", xid,
                                connectionAdapter.getRemoteAddress(), t.getMessage());
                        LOG.trace("DETAIL of sending of hello failure:", t);
                    }
                });

        LOG.debug("future features [{}] hooked ..", xid);
    }
    
}
