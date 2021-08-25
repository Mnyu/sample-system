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

package com.ntw.oms.order.dao.cassandra;

import com.ntw.oms.order.dao.cassandra.DBOrderKey;
import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.entity.OrderLine;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by anurag on 19/04/17.
 */
@Table("OrderLine")
public class DBOrderLine {
    @PrimaryKey
    private DBOrderKey orderKey;

    private String productId;
    private float quantity;

    public DBOrderKey getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(DBOrderKey orderKey) {
        this.orderKey = orderKey;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "{" +
                "\"orderKey\":" + (orderKey == null ? "null" : orderKey.toString()) + ", " +
                "\"productId\":" + (productId == null ? "null" : "\"" + productId + "\"") + ", " +
                "\"quantity\":\"" + quantity + "\"" +
                "}";
    }

    public static List<DBOrderLine> createDBOrder(Order order) {
        List<DBOrderLine> dbOrderLines = new LinkedList<>();
        for (OrderLine orderLine : order.getOrderLines()) {
            DBOrderKey orderKey = new DBOrderKey();
            orderKey.setUserId(order.getUserId());
            orderKey.setId(order.getId());
            orderKey.setOrderLineId(orderLine.getId());
            DBOrderLine dbOrderLine = new DBOrderLine();
            dbOrderLine.setOrderKey(orderKey);
            dbOrderLine.setProductId(orderLine.getProductId());
            dbOrderLine.setQuantity(orderLine.getQuantity());
            dbOrderLines.add(dbOrderLine);
        }
        return dbOrderLines;
    }

    public static Order getOrder(String userId, String id, List<DBOrderLine> dbOrderLines) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        if (dbOrderLines.size() > 0) {
            order.setId(dbOrderLines.get(0).getOrderKey().getId());
            order.setUserId(dbOrderLines.get(0).getOrderKey().getUserId());
        }
        for (DBOrderLine dbOrderLine : dbOrderLines) {
            OrderLine orderLine = new OrderLine();
            orderLine.setId(dbOrderLine.getOrderKey().getOrderLineId());
            orderLine.setProductId(dbOrderLine.getProductId());
            orderLine.setQuantity(dbOrderLine.getQuantity());
            order.getOrderLines().add(orderLine);
        }
        return order;
    }
}
