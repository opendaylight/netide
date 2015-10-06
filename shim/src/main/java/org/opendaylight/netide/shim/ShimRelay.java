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
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.protocol.impl.deserialization.NetIdeDeserializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.NetIdeSerializerRegistryImpl;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
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

/**
 * @author giuseppex.petralia@intel.com
 *
 */
public class ShimRelay {
    
    public  static void sendOpenFlowMessageToCore(ZeroMQBaseConnector coreConnector, DataObject msg, short ofVersion, long xId, long datapathId) {
        SerializationFactory factory = new SerializationFactory();
        SerializerRegistry registry = new NetIdeSerializerRegistryImpl();
        registry.init();
        ByteBuf output = UnpooledByteBufAllocator.DEFAULT.buffer();
        factory.setSerializerTable(registry);
        factory.messageToBuffer(ofVersion, output, msg);
        
        Message message = new Message(NetIPUtils.StubHeaderFromPayload(output.array()), output.array());
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
        
        // Deserialization
        DeserializationFactory factory = new DeserializationFactory();
        DeserializerRegistry registry = new NetIdeDeserializerRegistryImpl();
        registry.init();
        factory.setRegistry(registry);
        DataObject msg = factory.deserialize(input, ofVersion);
        
        // Send to Core and intercept response
        if (msg instanceof BarrierInput){
            Future<RpcResult<BarrierOutput>> reply = connectionAdapter.barrier((BarrierInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof EchoInput){
            Future<RpcResult<EchoOutput>> reply = connectionAdapter.echo((EchoInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof EchoReplyInput){
            connectionAdapter.echoReply((EchoReplyInput)msg);
        }else if (msg instanceof ExperimenterInput){
            connectionAdapter.experimenter((ExperimenterInput)msg);
        }else if (msg instanceof FlowModInput){
            connectionAdapter.flowMod((FlowModInput)msg);
        }else if (msg instanceof GetAsyncInput){
            Future<RpcResult<GetAsyncOutput>> reply = connectionAdapter.getAsync((GetAsyncInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof GetConfigInput){
            Future<RpcResult<GetConfigOutput>> reply = connectionAdapter.getConfig((GetConfigInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof GetFeaturesInput){
            Future<RpcResult<GetConfigOutput>> reply = connectionAdapter.getConfig((GetConfigInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof GetQueueConfigInput){
            Future<RpcResult<GetQueueConfigOutput>> reply = connectionAdapter.getQueueConfig((GetQueueConfigInput)msg);
            sendResponseToCore(reply, coreConnector, ofVersion, ((BarrierInput) msg).getXid(), datapathId);
        }else if (msg instanceof GroupModInput){
            connectionAdapter.groupMod((GroupModInput)msg);
        }else if (msg instanceof HelloInput){
            connectionAdapter.hello((HelloInput)msg);
        }else if (msg instanceof MeterModInput){
            connectionAdapter.meterMod((MeterModInput) msg);
        }else if (msg instanceof MultipartRequestInput){
            connectionAdapter.multipartRequest((MultipartRequestInput)msg);
        }else if (msg instanceof PacketOutInput){
            connectionAdapter.packetOut((PacketOutInput)msg);
        }else if (msg instanceof PortModInput){
            connectionAdapter.portMod((PortModInput) msg);
        }else if (msg instanceof SetAsyncInput){
            connectionAdapter.setAsync((SetAsyncInput)msg);
        }else if (msg instanceof SetConfigInput){
            connectionAdapter.setConfig((SetConfigInput)msg);
        }else if (msg instanceof TableModInput){
            connectionAdapter.tableMod((TableModInput)msg);
        }
                
    }
    
    private static <E extends DataObject> void sendResponseToCore(Future<RpcResult<E>> switchReply,
            final ZeroMQBaseConnector coreConnector, final short ofVersion, final long xId, final long datapathId) {
        Futures.addCallback(JdkFutureAdapters.listenInPoolThread(switchReply), new FutureCallback<RpcResult<E>>() {
            @Override
            public void onSuccess(RpcResult<E> rpcReply) {
                if (rpcReply.isSuccessful()) {
                    E result = rpcReply.getResult();
                    sendOpenFlowMessageToCore(coreConnector, result, ofVersion, xId, datapathId);
                } else {
                    for (RpcError rpcError : rpcReply.getErrors()) {
                        // TODO: Send Error to Core
                        /*
                         * LOG.debug(
                         * "handshake - features failure [{}]: i:{} | m:{} | s:{}"
                         * , xid, rpcError.getInfo(), rpcError.getMessage(),
                         * rpcError.getSeverity(), rpcError.getCause() );
                         */
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // TODO: Send Error to Core
            }
        });
    }
}
