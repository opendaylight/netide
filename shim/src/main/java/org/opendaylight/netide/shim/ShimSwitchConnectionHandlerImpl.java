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
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Future;
import java.util.ArrayList;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.SwitchConnectionHandler;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.NetIdeDeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.netide.netiplib.HelloMessage;
import org.opendaylight.netide.netiplib.Protocol;
import org.opendaylight.netide.netiplib.ProtocolVersions;
import org.javatuples.Pair;

public class ShimSwitchConnectionHandlerImpl implements SwitchConnectionHandler, ICoreListener, IHandshakeListener {
    public static final Long DEFAULT_XID = 0x01L;
    private static final Logger LOG = LoggerFactory.getLogger(
            ShimSwitchConnectionHandlerImpl.class);
    
    private static ZeroMQBaseConnector coreConnector;
    private ConnectionAdaptersRegistry connectionRegistry;
    private Pair<Protocol, ProtocolVersions> supportedProtocol;
    List<Pair<Protocol, ProtocolVersions>> supportedProtocols;
    private DeserializationFactory deserializationFactory;
    private DeserializerRegistry registry;
    
    public ShimSwitchConnectionHandlerImpl(ZeroMQBaseConnector connector) {
        coreConnector = connector;
        supportedProtocol = null;
        supportedProtocols = new ArrayList<>();
        supportedProtocols.add(new Pair<Protocol, ProtocolVersions>(
                Protocol.OPENFLOW, ProtocolVersions.OPENFLOW_1_0));
        supportedProtocols.add(new Pair<Protocol, ProtocolVersions>(
                Protocol.OPENFLOW, ProtocolVersions.OPENFLOW_1_3));
        deserializationFactory = new DeserializationFactory();
        registry = new NetIdeDeserializerRegistryImpl();
        deserializationFactory.setRegistry(registry);
        connectionRegistry = new ConnectionAdaptersRegistry();
        connectionRegistry.init();
    }

    @Override
    public boolean accept(InetAddress arg0) {
        return true;
    }

    @Override
    public void onSwitchConnected(ConnectionAdapter connectionAdapter) {
        LOG.info("SHIM: on Switch connected: {}", connectionAdapter.getRemoteAddress());
        ShimMessageListener listener = new ShimMessageListener(
                coreConnector, connectionAdapter);
        listener.registerConnectionAdaptersRegistry(connectionRegistry);
        listener.registerHandshakeListener(this);
        connectionRegistry.registerConnectionAdapter(connectionAdapter, null);
        connectionAdapter.setMessageListener(listener);
        connectionAdapter.setSystemListener(listener);
        connectionAdapter.setConnectionReadyListener(listener);
        handshake(connectionAdapter);
    }
    
    public void handshake(ConnectionAdapter connectionAdapter){
        LOG.info("SHIM: OF Handshake Switch: {}", connectionAdapter.getRemoteAddress());
        HelloInputBuilder builder = new HelloInputBuilder();
        builder.setVersion((short)getMaxOFSupportedProtocol());
        builder.setXid(DEFAULT_XID);
        List<Elements> elements = new ArrayList<Elements>();
        builder.setElements(elements);
        connectionAdapter.hello(builder.build());
    }
    
    @Override
    public void onSwitchHelloMessage(long xid, Short version){
        LOG.info("SHIM: OpenFlow hello message received. Xid: {}, OFVersion: {}", xid, version);
        byte received = version.byteValue();
        if (xid >= DEFAULT_XID){
            if (received <=  getMaxOFSupportedProtocol()){
                LOG.info("SHIM: OpenFlow handshake agreed on version: {}", received);
                setSupportedProtocol(received);
                
            }else{
                LOG.info("SHIM: OpenFlow handshake agreed on version: {}", getMaxOFSupportedProtocol());
                setSupportedProtocol(getMaxOFSupportedProtocol());
            }
        }
    }
    
    public byte getMaxOFSupportedProtocol(){
        byte max = 0x00;
        for (Pair<Protocol,ProtocolVersions> protocols : this.supportedProtocols){
            if (protocols.getValue0() == Protocol.OPENFLOW && 
                    protocols.getValue1().getValue() > max){
                max = protocols.getValue1().getValue();
            }
        }
        return max;
    }
    
    public void setSupportedProtocol(byte version){
        this.supportedProtocol = new Pair<Protocol, ProtocolVersions>(
                Protocol.OPENFLOW,
                ProtocolVersions.parse(Protocol.OPENFLOW, version));
    }
    
    @Override
    public void onOpenFlowCoreMessage(Long datapathId, ByteBuf msg) {
        LOG.info("SHIM: OpenFlow Core message received");
        ConnectionAdapter conn = connectionRegistry.getConnectionAdapter(datapathId);
        if ( conn != null){
            LOG.info("SHIM: OpenFlow Core message ");
            short ofVersion = msg.readUnsignedByte();
            ShimRelay.sendToSwitch(conn, msg, ofVersion, coreConnector, datapathId);
        }
    }
    
    @Override
    public void onHelloCoreMessage(List<Pair<Protocol, ProtocolVersions>> requestedProtocols) {
        LOG.info("SHIM: Hello Core message received. Pair0: {}", requestedProtocols.get(0));
        for (Pair<Protocol, ProtocolVersions> requested : requestedProtocols){
            if( requested.getValue0().getValue() == supportedProtocol.getValue0().getValue() 
                    && requested.getValue1().getValue() == supportedProtocol.getValue1().getValue()){
                LOG.info("SHIM: OF version matched");
                HelloMessage msg = new HelloMessage();
                
                msg.getSupportedProtocols().add(supportedProtocol);
                msg.getHeader().setPayloadLength((short)2);
                coreConnector.SendData(msg.toByteRepresentation());
                for (ConnectionAdapter conn : connectionRegistry.getConnectionAdapters()){
                    LOG.info("SHIM: SendFeatures To core for switch: {}", conn.getRemoteAddress());
                    sendFeaturesToCore((short)supportedProtocol.getValue1().getValue(), 0L, conn);
                }
            }
        }
    }
    
    private void sendFeaturesToCore(final Short proposedVersion, final Long xid,
            final ConnectionAdapter connectionAdapter) {
        LOG.info("version set: {}", proposedVersion);
        // request features
        GetFeaturesInputBuilder featuresBuilder = new GetFeaturesInputBuilder();
        featuresBuilder.setVersion(proposedVersion).setXid(xid);
        LOG.info("sending feature request for version={} and xid={}", proposedVersion, xid);
        Future<RpcResult<GetFeaturesOutput>> featuresFuture = connectionAdapter
                .getFeatures(featuresBuilder.build());

        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(featuresFuture),
                new FutureCallback<RpcResult<GetFeaturesOutput>>() {
                    @Override
                    public void onSuccess(RpcResult<GetFeaturesOutput> rpcFeatures) {
                        LOG.info("features are back");
                        if (rpcFeatures.isSuccessful()) {
                            GetFeaturesOutput featureOutput = rpcFeatures.getResult();

                            LOG.info("obtained features: datapathId={}", featureOutput.getDatapathId());
                            
                            // Register Switch connection/DatapathId to registry
                            connectionRegistry.registerConnectionAdapter(connectionAdapter,
                                    featureOutput.getDatapathId());
                            // Send Feature reply to Core
                            ShimRelay.sendOpenFlowMessageToCore( ShimSwitchConnectionHandlerImpl.coreConnector, 
                                    featureOutput, proposedVersion, xid, featureOutput.getDatapathId().shortValue());
                            
                        } else {
                            // Handshake failed
                            LOG.info("issuing disconnect during handshake [{}]", 
                                    connectionAdapter.getRemoteAddress());
                            
                            for (RpcError rpcError : rpcFeatures.getErrors()) {
                                LOG.info("handshake - features failure [{}]: i:{} | m:{} | s:{}", xid,
                                        rpcError.getInfo(), rpcError.getMessage(), rpcError.getSeverity(),
                                        rpcError.getCause());
                            }
                        }

                        LOG.info("postHandshake DONE");
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        LOG.info("getting feature failed seriously [{}, addr:{}]: {}", xid,
                                connectionAdapter.getRemoteAddress(), t.getMessage());
                        LOG.info("DETAIL of sending of hello failure:", t);
                    }
                });

        LOG.info("future features [{}] hooked ..", xid);
    }
    
}