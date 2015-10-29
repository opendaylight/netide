package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.main.impl.rev151001.modules.module.configuration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.main.impl.rev151001.modules.module.configuration.main.impl.Broker;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.rev130405.modules.module.Configuration;
import org.opendaylight.yangtools.yang.binding.Augmentable;


/**
 * &lt;p&gt;This class represents the following YANG schema fragment defined in module &lt;b&gt;main-impl&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/main-impl.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * case main-impl {
 *     leaf core-port {
 *         type int32;
 *     }
 *     container broker {
 *         leaf type {
 *             type leafref;
 *         }
 *         leaf name {
 *             type leafref;
 *         }
 *         uses service-ref {
 *             refine (urn:opendaylight:params:xml:ns:yang:netide:main-impl?revision=2015-10-01)type {
 *                 leaf type {
 *                     type leafref;
 *                 }
 *             }
 *         }
 *     }
 * }
 * &lt;/pre&gt;
 * The schema path to identify an instance is
 * &lt;i&gt;main-impl/modules/module/configuration/(urn:opendaylight:params:xml:ns:yang:netide:main-impl?revision=2015-10-01)main-impl&lt;/i&gt;
 *
 */
public interface MainImpl
    extends
    DataObject,
    Augmentable<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.main.impl.rev151001.modules.module.configuration.MainImpl>,
    Configuration
{



    public static final QName QNAME = org.opendaylight.yangtools.yang.common.QName.cachedReference(org.opendaylight.yangtools.yang.common.QName.create("urn:opendaylight:params:xml:ns:yang:netide:main-impl","2015-10-01","main-impl"));

    java.lang.Integer getCorePort();
    
    Broker getBroker();

}

