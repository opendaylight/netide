package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001;

import java.util.List;

import com.google.common.base.MoreObjects;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opendaylight.netide.impl.NetideProvider;
import org.opendaylight.netide.shim.ShimConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.ThreadConfiguration;
import org.opendaylight.openflowjava.protocol.api.connection.TlsConfiguration;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.KeystoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.TransportProtocol;

public class NetideMainModule extends
        org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001.AbstractNetideMainModule {
    public NetideMainModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NetideMainModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001.NetideMainModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        NetideProvider provider = null;
        try {
            InetAddress coreAddress = extractIpAddressBin(getCoreAddress());
            int corePort = getCorePort();
            ConnectionConfiguration conf = createConnectionConfiguration();
            provider = new NetideProvider(coreAddress.getHostAddress(), corePort, conf,
                    getNotificationPublishAdapterDependency());
            getBrokerDependency().registerProvider(provider);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return provider;
    }

    private ConnectionConfiguration createConnectionConfiguration() throws UnknownHostException {
        final InetAddress address = extractIpAddressBin(getAddress());
        final Integer port = getPort();
        final long switchIdleTimeout = getSwitchIdleTimeout();
        final Tls tls = getTls();
        final Threads threads = getThreads();
        final TransportProtocol transportProtocol = getTransportProtocol();
        ThreadConfiguration threadsConfig = null;

        if (threads != null) {
            threadsConfig = new ThreadConfiguration() {

                @Override
                public int getWorkerThreadCount() {
                    return threads.getWorkerThreads();
                }

                @Override
                public int getBossThreadCount() {
                    return threads.getBossThreads();
                }
            };
        }

        TlsConfiguration tlsConfig = null;

        if (tls != null) {
            tlsConfig = new TlsConfiguration() {
                @Override
                public KeystoreType getTlsTruststoreType() {
                    return MoreObjects.firstNonNull(tls.getTruststoreType(), null);
                }

                @Override
                public String getTlsTruststore() {
                    return MoreObjects.firstNonNull(tls.getTruststore(), null);
                }

                @Override
                public KeystoreType getTlsKeystoreType() {
                    return MoreObjects.firstNonNull(tls.getKeystoreType(), null);
                }

                @Override
                public String getTlsKeystore() {
                    return MoreObjects.firstNonNull(tls.getKeystore(), null);
                }

                @Override
                public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType getTlsKeystorePathType() {
                    return MoreObjects.firstNonNull(tls.getKeystorePathType(), null);
                }

                @Override
                public org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.config.rev140630.PathType getTlsTruststorePathType() {
                    return MoreObjects.firstNonNull(tls.getTruststorePathType(), null);
                }

                @Override
                public String getKeystorePassword() {
                    return MoreObjects.firstNonNull(tls.getKeystorePassword(), null);
                }

                @Override
                public String getCertificatePassword() {
                    return MoreObjects.firstNonNull(tls.getCertificatePassword(), null);
                }

                @Override
                public String getTruststorePassword() {
                    return MoreObjects.firstNonNull(tls.getTruststorePassword(), null);
                }

                @Override
                public List<String> getCipherSuites() {
                  return MoreObjects.firstNonNull(tls.getCipherSuites(), null);
                }
            };
        }

        return new ShimConnectionConfiguration(address, port, switchIdleTimeout, threadsConfig, tlsConfig,
                transportProtocol);
    }

    private static InetAddress extractIpAddressBin(final IpAddress address) throws UnknownHostException {

        if (address != null) {
            if (address.getIpv4Address() != null) {
                return InetAddress.getByName(address.getIpv4Address().getValue());
            } else if (address.getIpv6Address() != null) {
                return InetAddress.getByName(address.getIpv6Address().getValue());
            }
        }
        return null;
    }
}