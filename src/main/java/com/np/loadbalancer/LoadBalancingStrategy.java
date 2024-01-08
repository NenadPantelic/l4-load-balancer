package com.np.loadbalancer;

public enum LoadBalancingStrategy {

    ROUND_ROBIN,
    WEIGHTED_ROUND_ROBIN,
    LEAST_CONNECTIONS,
    IP_HASHING,
    CONSISTENT_HASHING
}
