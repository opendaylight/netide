package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netide.rev151001;
import java.util.concurrent.Future;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.RpcResult;


/**
 * Interface for implementing the following YANG RPCs defined in module &lt;b&gt;netide&lt;/b&gt;
 * &lt;br&gt;(Source path: &lt;i&gt;META-INF/yang/netide.yang&lt;/i&gt;):
 * &lt;pre&gt;
 * rpc status {
 *     output {
 *         leaf netip-version {
 *             type int8;
 *         }
 *     }
 *     status CURRENT;
 * }
 * &lt;/pre&gt;
 *
 */
public interface NetideService
    extends
    RpcService
{




    Future<RpcResult<StatusOutput>> status();

}

