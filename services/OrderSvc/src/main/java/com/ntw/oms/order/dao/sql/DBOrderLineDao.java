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

package com.ntw.oms.order.dao.sql;

import com.ntw.oms.order.dao.OrderDao;
import com.ntw.oms.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by anurag on 24/03/17.
 */
@Component("SQL")
public class DBOrderLineDao implements OrderDao {

    @Autowired(required = false)
    @Qualifier("orderJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DBOrderLineDao.class);

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String GET_ORDER_SQL = "select * from orderline where orderId=?";

    @Override
    public Order getOrder(String userId, String id) {
        List<DBOrderLine> dbOrderLines;
        try {
            dbOrderLines = jdbcTemplate.query(GET_ORDER_SQL, new Object[]{id},
                    new BeanPropertyRowMapper<>(DBOrderLine.class));
        } catch (Exception e) {
            logger.error("Exception while fetching order from db; id={}", id);
            logger.error("Exception message: ", e);
            return null;
        }
        if (dbOrderLines.isEmpty()) {
            logger.debug("No order lines found; userId={}", GET_ORDER_SQL);
            return null;
        }
        Order order = DBOrderLine.getOrder(userId, id, dbOrderLines);
        logger.debug("Fetched order; context={}", order);
        return order;
    }

    private static final String GET_ORDERS_SQL = "select * from orderline where userId=?";

    @Override
    public List<Order> getOrders(String userId) {
        List<DBOrderLine> dbOrderLines;
        try {
            dbOrderLines = jdbcTemplate.query(GET_ORDERS_SQL, new Object[]{userId},
                    new BeanPropertyRowMapper<>(DBOrderLine.class));
        } catch (Exception e) {
            logger.error("Exception while fetching orders from db");
            logger.error("Exception message: ", e);
            return null;
        }
        if (dbOrderLines.isEmpty()) {
            logger.debug("No order lines found; userId={}", GET_ORDERS_SQL);
            return new LinkedList<>();
        }
        List<Order> orders = getOrders(dbOrderLines);
        logger.debug("Fetched orders; context={}", orders);
        return orders;
    }

    private static final String ORDER_INSERT_SQL =
            "insert into OrderLine (orderId, orderLineId, productId, quantity, userId) values(?,?,?,?,?)";

    @Override
    public boolean saveOrder(Order order) {
        List<DBOrderLine> dbOrderLines = DBOrderLine.createDBOrder(order);
        int[] updateStatusArr;
        try {
            updateStatusArr = jdbcTemplate.batchUpdate(ORDER_INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i)
                        throws SQLException {
                    ps.setString(1, dbOrderLines.get(i).getOrderId());
                    ps.setInt(2, dbOrderLines.get(i).getOrderLineId());
                    ps.setString(3, dbOrderLines.get(i).getProductId());
                    ps.setFloat(4, dbOrderLines.get(i).getQuantity());
                    ps.setString(5, dbOrderLines.get(i).getUserId());
                }

                @Override
                public int getBatchSize() {
                    return dbOrderLines.size();
                }
            });
        } catch (Exception e) {
            logger.error("Unable to save order; context={}", order);
            logger.error("Exception message: ", e);
            return false;
        }
        if (updateStatusArr.length == 0) {
            logger.error("Unable to save order; context={}", order);
            return false;
        }
        logger.debug("Saved order; context={}", order);
        return true;
    }

    private static final String DELETE_ORDER_SQL = "delete from orderline where orderId=?";

    @Override
    public boolean removeOrder(String userId, String id) {
        try {
            jdbcTemplate.update(DELETE_ORDER_SQL, new Object[]{id});
        } catch (Exception e) {
            logger.error("Unable to delete order; userId={}, id={}", userId, id);
            logger.error("Exception message: ", e);
            return false;
        }
        logger.debug("Removed order; orderId={}", id);
        return true;
    }

    private static final String DELETE_ORDERS_SQL = "delete from orderline";

    @Override
    public boolean removeOrders() {
        try {
        jdbcTemplate.update(DELETE_ORDERS_SQL);
        } catch (Exception e) {
            logger.error("Unable to delete orders");
            logger.error("Exception message: ", e);
            return false;
        }
        logger.debug("Removed all orders");
        return true;
    }

    private List<Order> getOrders(List<DBOrderLine> dbOrderLines) {
        Map<String,List<DBOrderLine>> orderMap = new HashMap<>(100);
        for (DBOrderLine dbOrderLine : dbOrderLines) {
            String orderId = dbOrderLine.getOrderId();
            List<DBOrderLine> dbUserOrderLines = orderMap.get(orderId);
            if (dbUserOrderLines == null) {
                dbUserOrderLines = new LinkedList<>();
                orderMap.put(orderId, dbUserOrderLines);
            }
            dbUserOrderLines.add(dbOrderLine);
        }
        List<Order> orderList = new LinkedList<>();
        for (String orderId : orderMap.keySet()) {
            List<DBOrderLine> dbUserOrderLines = orderMap.get(orderId);
            Order order = DBOrderLine.getOrder(dbUserOrderLines.get(0).getUserId(),
                    dbUserOrderLines.get(0).getOrderId(), dbUserOrderLines);
            orderList.add(order);
        }
        return orderList;
    }
}
