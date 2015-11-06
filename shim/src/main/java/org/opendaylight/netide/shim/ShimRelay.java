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
import io.netty.buffer.UnpooledByteBufAllocator;
import java.util.concurrent.Future;
import org.opendaylight.netide.netiplib.Message;
import org.opendaylight.netide.netiplib.MessageType;
import org.opendaylight.netide.netiplib.NetIPUtils;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.extensibility.DeserializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.NetIdeDeserializationFactory;
import org.opendaylight.netide.openflowjava.protocol.impl.deserialization.NetIdeDeserializerRegistryImpl;
import org.opendaylight.netide.openflowjava.protocol.impl.serialization.NetIdeSerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ShimRelay {
    private static final Logger LOG = LoggerFactory.getLogger(ShimRelay.class);
    
    public  static void sendOpenFlowMessageToCore(ZeroMQBaseConnector coreConnector, DataObject msg, short ofVersion, long xId, long datapathId) {
        LOG.info("SHIM RELAY: sending message to core");
        SerializationFactory factory = new SerializationFactory();
        SerializerRegistry registry = new NetIdeSerializerRegistryImpl();
        registry.init();
        ByteBuf output = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.setSerializerTable(registry);
        factory.messageToBuffer(ofVersion, output, msg);
        byte[] bytes = new byte[output.readableBytes()];
        output.readBytes(bytes);
        Message message = new Message(NetIPUtils.StubHeaderFromPayload(bytes), bytes);
        message.getHeader().setMessageType(MessageType.OPENFLOW);
        message.getHeader().setDatapathId(datapathId);
        // TODO: find the correct values
        message.getHeader().setModuleId(0);
        message.getHeader().setTransactionId((int)xId);
        coreConnector.SendData(message.toByteRepresentation());
    }
    
    public static void sendToSwitch(ConnectionAdapter connectionAdapter, 
            ByteBuf input, short ofVersion, 
            ZeroMQBaseConnector coreConnector, long datapathId){
        LOG.info("SHIM RELAY: sending bytebuf to switch");
        NetIdeDeserializationFactory factory = new NetIdeDeserializationFactory();
        DeserializerRegistry registry = new NetIdeDeserializerRegistryImpl();
        registry.init();
        factory.setRegistry(registry);
        DataObject msg = factory.deserialize(input, ofVersion);
        ShimRelay.sendDataObjectToSwitch(connectionAdapter, msg, ofVersion, coreConnector, datapathId);
    }
    
    public static void sendDataObjectToSwitch(ConnectionAdapter connectionAdapter, 
            DataObject msg, short ofVersion, 
            ZeroMQBaseConnector coreConnector, long datapathId){
        
        LOG.info("SHIM RELAY: sending dataObject to switch");
        // Send to Core and intercept response
        if (msg.getImplementedInterface().getClass().getName().equals(BarrierInput.class.getName())){
            LOG.info("SHIM RELAY: sending BarrierInput to switch");
            Future<RpcResult<BarrierOutput>> reply = connectionAdapter.barrier((BarrierInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(EchoInput.class.getName())){
            LOG.info("SHIM RELAY: sending EchoInput to switch");
            Future<RpcResult<EchoOutput>> reply = connectionAdapter.echo((EchoInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((EchoInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(EchoOutput.class.getName())){
            EchoReplyInputBuilder builder = new EchoReplyInputBuilder();
            builder.setVersion(((EchoOutput)msg).getVersion());
            builder.setXid(((EchoOutput)msg).getXid());
            builder.setData(((EchoOutput)msg).getData());
            LOG.info("SHIM RELAY: sending EchoReplyInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.echoReply(builder.build());
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(ExperimenterInput.class.getName())){
            LOG.info("SHIM RELAY: sending ExperimenterInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.experimenter((ExperimenterInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(FlowModInput.class.getName())){
            LOG.info("SHIM RELAY: sending FlowModInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.flowMod((FlowModInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(GetAsyncInput.class.getName())){
            LOG.info("SHIM RELAY: sending GetAsyncInput to switch");
            Future<RpcResult<GetAsyncOutput>> reply = connectionAdapter.getAsync((GetAsyncInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((GetAsyncInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(GetConfigInput.class.getName())){
            LOG.info("SHIM RELAY: sending GetConfigInput to switch");
            Future<RpcResult<GetConfigOutput>> reply = connectionAdapter.getConfig((GetConfigInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((GetConfigInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(GetFeaturesInput.class.getName())){
            LOG.info("SHIM RELAY: sending GetFeaturesInput to switch");
            Future<RpcResult<GetFeaturesOutput>> reply = connectionAdapter.getFeatures((GetFeaturesInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((GetFeaturesInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(GetQueueConfigInput.class.getName())){
            LOG.info("SHIM RELAY: sending GetQueueConfigInput to switch");
            Future<RpcResult<GetQueueConfigOutput>> reply = connectionAdapter.getQueueConfig((GetQueueConfigInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((GetQueueConfigInput) msg).getXid(), datapathId);
        }else if (msg.getImplementedInterface().getName().equals(GroupModInput.class.getName())){
            LOG.info("SHIM RELAY: sending GroupModInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.groupMod((GroupModInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(HelloInput.class.getName())){
            LOG.info("SHIM RELAY: sending HelloInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.hello((HelloInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(MeterModInput.class.getName())){
            LOG.info("SHIM RELAY: sending MeterModInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.meterMod((MeterModInput) msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(MultipartRequestInput.class.getName())){
            LOG.info("SHIM RELAY: sending MultipartRequestInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.multipartRequest((MultipartRequestInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(PacketOutInput.class.getName())){
            LOG.info("SHIM RELAY: sending PacketOutInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.packetOut((PacketOutInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(PortModInput.class.getName())){
            LOG.info("SHIM RELAY: sending PortModInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.portMod((PortModInput) msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(SetAsyncInput.class.getName())){
            LOG.info("SHIM RELAY: sending SetAsyncInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.setAsync((SetAsyncInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(SetConfigInput.class.getName())){
            LOG.info("SHIM RELAY: sending SetConfigInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.setConfig((SetConfigInput)msg);
            logRpcWithNoResponse(reply);
        }else if (msg.getImplementedInterface().getName().equals(TableModInput.class.getName())){
            LOG.info("SHIM RELAY: sending TableModInput to switch");
            Future<RpcResult<Void>> reply = connectionAdapter.tableMod((TableModInput)msg);
            logRpcWithNoResponse(reply);
        }else{
            LOG.info("SHIM RELAY:Dataobject not recognized");
        }
            
                
    }
    
    private static void logRpcWithNoResponse(Future<RpcResult<Void>> switchReply) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(switchReply), new FutureCallback<RpcResult<Void>>() {
            @Override
            public void onSuccess(RpcResult<Void> rpcReply) {
                if (rpcReply.isSuccessful()) {
                    LOG.info("SHIM RELAY: packetOut success");
                    
                } else {
                    for (RpcError rpcError : rpcReply.getErrors()) {
                        LOG.info("SHIM RELAY: packetout error: " + rpcError.getMessage());
                    }
                }
            }
            @Override
            public void onFailure(Throwable t) {
                LOG.info("SHIM RELAY: packetout failure");
            }
        });
    }
    
    private static <E extends DataObject> void sendResponseToCore(Future<RpcResult<E>> switchReply,
            final ZeroMQBaseConnector coreConnector, final short ofVersion, final long xId, final long datapathId) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(switchReply), new FutureCallback<RpcResult<E>>() {
            @Override
            public void onSuccess(RpcResult<E> rpcReply) {
                if (rpcReply.isSuccessful()) {
                    E result = rpcReply.getResult();
                    LOG.info("SHIM RELAY: sending Response to switch. Class: {}", result.getClass());
                    sendOpenFlowMessageToCore(coreConnector, result, ofVersion, xId, datapathId);
                } else {
                    for (RpcError rpcError : rpcReply.getErrors()) {
                        LOG.info("SHIM RELAY: error in communication with switch: {}", rpcError.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.info("SHIM RELAY: failure on communication with switch");
            }
        });
    }
}
