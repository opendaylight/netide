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
import org.opendaylight.openflowjava.protocol.impl.deserialization.NetIdeDeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.netide.netiplib.HelloMessage;
import org.opendaylight.netide.netiplib.Protocol;
import org.opendaylight.netide.netiplib.ProtocolVersions;
import org.javatuples.Pair;

public class ShimSwitchConnectionHandlerImpl implements SwitchConnectionHandler, ICoreListener {
    private static final Logger LOG = LoggerFactory.getLogger(
            ShimSwitchConnectionHandlerImpl.class);
    
    private static ZeroMQBaseConnector coreConnector;
    private ConnectionAdaptersRegistry connectionRegistry;
    Pair<Protocol, ProtocolVersions> supportedProtocol;
    private DeserializationFactory deserializationFactory;
    private DeserializerRegistry registry;
    
    public ShimSwitchConnectionHandlerImpl(ZeroMQBaseConnector connector) {
        coreConnector = connector;
        
        supportedProtocol = new Pair<Protocol, ProtocolVersions>(
                Protocol.OPENFLOW, ProtocolVersions.OPENFLOW_1_3);
        
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
        LOG.info("SHIM: on Switch connected: ", connectionAdapter.getRemoteAddress());
        ShimMessageListener listener = new ShimMessageListener(
                coreConnector, connectionAdapter);
        listener.registerConnectionAdaptersRegistry(connectionRegistry);
        connectionRegistry.registerConnectionAdapter(connectionAdapter, null);
        connectionAdapter.setMessageListener(listener);
        connectionAdapter.setSystemListener(listener);
        connectionAdapter.setConnectionReadyListener(listener);
    }

    @Override
    public void onOpenFlowCoreMessage(Long datapathId, ByteBuf input) {
        LOG.info("SHIM: OpenFlow Core message received");
        ConnectionAdapter conn = connectionRegistry.getConnectionAdapter(datapathId);
        if ( conn != null){
            ShimRelay.sendToSwitch(conn, input, 
                    this.supportedProtocol.getValue1().getValue(), 
                    ShimSwitchConnectionHandlerImpl.coreConnector, datapathId);
        }
    }
    
    @Override
    public void onHelloCoreMessage(List<Pair<Protocol, ProtocolVersions>> requestedProtocols) {
        LOG.info("SHIM: Hello Core message received");
        for (Pair<Protocol, ProtocolVersions> requested : requestedProtocols){
            if( requested == supportedProtocol){
                HelloMessage msg = new HelloMessage();
                List<Pair<Protocol, ProtocolVersions>> supportedProtocols = new ArrayList<>();
                supportedProtocols.add(supportedProtocol);
                msg.setSupportedProtocols(supportedProtocols);
                coreConnector.SendData(msg.toByteRepresentation());
                for (ConnectionAdapter conn : connectionRegistry.getConnectionAdapters()){
                    sendFeaturesToCore((short)supportedProtocol.getValue1().getValue(), 0L, conn);
                }
            }
        }
    }
    
    private void sendFeaturesToCore(final Short proposedVersion, final Long xid,
            final ConnectionAdapter connectionAdapter) {
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

                            LOG.debug("obtained features: datapathId={}", featureOutput.getDatapathId());
                            
                            // Register Switch connection/DatapathId to registry
                            connectionRegistry.registerConnectionAdapter(connectionAdapter,
                                    featureOutput.getDatapathId());
                            
                            // Send Feature reply to Core
                            ShimRelay.sendOpenFlowMessageToCore( ShimSwitchConnectionHandlerImpl.coreConnector, 
                                    featureOutput, proposedVersion, xid, featureOutput.getDatapathId().shortValue());
                            
                        } else {
                            // handshake failed
                            LOG.warn("issuing disconnect during handshake [{}]", 
                                    connectionAdapter.getRemoteAddress());
                            
                            for (RpcError rpcError : rpcFeatures.getErrors()) {
                                LOG.debug("handshake - features failure [{}]: i:{} | m:{} | s:{}", xid,
                                        rpcError.getInfo(), rpcError.getMessage(), rpcError.getSeverity(),
                                        rpcError.getCause());
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