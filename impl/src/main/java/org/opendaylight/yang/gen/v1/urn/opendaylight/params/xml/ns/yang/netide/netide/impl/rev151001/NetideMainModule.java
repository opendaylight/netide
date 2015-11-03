package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001;

import org.opendaylight.netide.impl.NetideProvider;

public class NetideMainModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001.AbstractNetideMainModule {
    public NetideMainModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NetideMainModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.netide.impl.rev151001.NetideMainModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        NetideProvider provider = new NetideProvider(getCorePort());
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
