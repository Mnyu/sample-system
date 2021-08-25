//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               //
//                                                                          //
// Licensed under the Apache License, Version 2.0 (the "License");          //
// you may not use this file except in compliance with the License.         //
// You may obtain a copy of the License at                                  //
//                                                                          //
//     http://www.apache.org/licenses/LICENSE-2.0                           //
//                                                                          //
// Unless required by applicable law or agreed to in writing, software      //
// distributed under the License is distributed on an "AS IS" BASIS,        //
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. //
// See the License for the specific language governing permissions and      //
// limitations under the License.                                           //
//////////////////////////////////////////////////////////////////////////////

package com.ntw.oms.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntw.common.config.AppConfig;
import com.ntw.common.config.ServiceID;
import com.ntw.common.http.HttpClient;
import com.ntw.common.http.HttpClientResponse;
import com.ntw.oms.order.entity.InventoryReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by anurag on 22/05/19.
 */
@Component
@RibbonClient(name = "InventoryClient")
public class InventoryClientImpl implements InventoryClient {

    @Value("${InventorySvc.client.cp.size:10}")
    private int httpClientPoolSize;

    @Autowired
    private LoadBalancerClient loadBalancer;

    private static HttpClient client;

    @PostConstruct
    public void postConstruct() {
        client = new HttpClient(httpClientPoolSize);
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    public LoadBalancerClient getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public boolean reserveInventory(InventoryReservation inventoryReservation, String authHeader) throws IOException {
        HttpClientResponse response;
        ObjectMapper mapper = new ObjectMapper();
        try {
            ServiceInstance instance = getLoadBalancer().choose(ServiceID.InventorySvc.toString());
            String host = instance.getHost();
            int port = instance.getPort();
            String uri = new StringBuilder()
                    .append(AppConfig.INVENTORY_RESOURCE_PATH).append("/")
                    .append(AppConfig.INVENTORY_RESERVATION_PATH).toString();
            response = client.sendPost(host, port, uri,
                    authHeader, MediaType.APPLICATION_JSON_VALUE, mapper.writeValueAsString(inventoryReservation));
        } catch (IOException e) {
            logger.error("Error calling InventorySvc while reserving inventory; context={}", inventoryReservation);
            logger.error(e.getMessage(), e);
            throw e;
        }
        if (response.getBody().equals("SUCCESS")) {
            logger.debug("Reserved inventory successfully; context={}", inventoryReservation);
            return true;
        }
        logger.debug("Unable to reserve inventory; context={}", inventoryReservation);
        return false;
    }
}
