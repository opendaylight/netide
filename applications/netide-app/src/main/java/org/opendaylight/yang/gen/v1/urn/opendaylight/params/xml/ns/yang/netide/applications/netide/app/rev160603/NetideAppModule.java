package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.applications.netide.app.rev160603;

import org.opendaylight.netide.applications.netideapp.NetideApp;

public class NetideAppModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.applications.netide.app.rev160603.AbstractNetideAppModule {
    public NetideAppModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NetideAppModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.applications.netide.app.rev160603.NetideAppModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final NetideApp netideApp = new NetideApp();

        return new AutoCloseable() {
            @Override
            public void close() {
                netideApp.close();
            }
        };
    }

}
