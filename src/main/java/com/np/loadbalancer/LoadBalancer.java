package com.np.loadbalancer;

import com.np.data.ClientContext;
import com.np.data.Server;

public interface LoadBalancer {

    /**
     * Returns the strategy used by load balancer.
     *
     * @return strategy used to balance the load and route the traffic.
     */
    LoadBalancingStrategy getStrategy();

    /**
     * Routes the traffic to some server behind the load balancer.
     *
     * @param clientContext details of the client making the request
     * @return Server where to route the request.
     */
    Server route(ClientContext clientContext);
}
