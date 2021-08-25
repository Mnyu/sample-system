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

import com.ntw.oms.cart.entity.Cart;
import com.ntw.oms.cart.entity.CartLine;
import com.ntw.oms.cart.service.CartServiceImpl;
import com.ntw.oms.order.dao.OrderDao;
import com.ntw.oms.order.dao.OrderDaoFactory;
import com.ntw.oms.order.entity.InventoryReservation;
import com.ntw.oms.order.entity.Order;
import com.ntw.oms.order.entity.OrderLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by anurag on 12/05/17.
 */

/**
 * Provides implementation of Cart Service methods
 */
@Component
public class OrderServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderDaoFactory orderDaoFactory;

    @Autowired
    private InventoryClient inventoryClientBean;

    private OrderDao orderDaoBean;

    @Autowired
    private CartServiceImpl cartServiceBean;

    @Value("${database.type}")
    private String orderDBType;

    @PostConstruct
    public void postConstruct()
    {
        this.orderDaoBean = orderDaoFactory.getOrderDao(orderDBType);
    }

    public InventoryClient getInventoryClientBean() {
        return inventoryClientBean;
    }

    public void setInventoryClientBean(InventoryClient inventoryClientBean) {
        this.inventoryClientBean = inventoryClientBean;
    }

    public OrderDao getOrderDaoBean() {
        return orderDaoBean;
    }

    public void setOrderDaoBean(OrderDao orderDaoBean) {
        this.orderDaoBean = orderDaoBean;
    }

    public CartServiceImpl getCartServiceBean() {
        return cartServiceBean;
    }

    public void setCartServiceBean(CartServiceImpl cartServiceBean) {
        this.cartServiceBean = cartServiceBean;
    }

    /**
     * @param userId    id of user who created the order
     * @param id        unique id of Order
     * @return          Order object
     */
    public Order fetchOrder(String userId, String id) {
        Order order = getOrderDaoBean().getOrder(userId, id);
        logger.debug("Fetched order; context={}", order);
        return order;
    }

    /**
     * @param userId    id of user who created the order
     * @return          List of Order objects
     */
    public List<Order> getOrders(String userId) {
        List<Order> orders = getOrderDaoBean().getOrders(userId);
        logger.debug("Fetched order; context={}", orders);
        return orders;
    }

    /**
     *
     * @param order      order object to be persisted
     * @return
     */
    public boolean saveOrder(Order order) {
        return getOrderDaoBean().saveOrder(order);
    }

    /**
     *
     * @param id        unique id of order to be removed
     * @return
     */
    public boolean removeOrder(String userId, String id) {
        return getOrderDaoBean().removeOrder(userId, id);
    }

    /**
     *
     * @param cartId    Id of User who created the order
     * @return          created order
     */
    public Order createOrder(String cartId, String authHeader) throws IOException {
        // get items from the cart
        Cart cart = getCartServiceBean().getCart(cartId);
        if (cart == null || cart.getCartLines().size() == 0) {
            logger.warn("Cannot create order as cart is empty; context={}", cartId);
            return null;
        }
        logger.debug("Fetched cart; context={}", cart);
        // create order from cart
        String orderId = createOrderId();
        Order order = new Order(orderId, cartId);
        List<OrderLine> orderLines = new LinkedList<OrderLine>();
        for (CartLine cartLine : cart.getCartLines()) {
            OrderLine orderLine = new OrderLine();
            orderLine.setId(cartLine.getId());
            orderLine.setProductId(cartLine.getProductId());
            orderLine.setQuantity(cartLine.getQuantity());
            orderLines.add(orderLine);
        }
        order.setOrderLines(orderLines);
        logger.debug("Prepared order; context={}", order);
        // reserve inventory
        if (! reserveInventory(order, authHeader)) {
            logger.error("Unable to reserve inventory for order; context={}", order);
            return null;
        }
        // empty cart
        if(! getCartServiceBean().removeCart(cartId)) {
            logger.error("Cannot empty cart; context={}", cartId);
        }
        logger.debug("Removed cart; context={}", cart);
        // persist order
        if (!saveOrder(order)) {
            logger.error("Unable to create order; context={}", order);
            // ToDo: Async Rollback inventory reservations
        }
        logger.debug("Created order; context={}", order);
        return order;
    }

    private boolean reserveInventory(Order order, String authHeader) throws IOException {
        List<OrderLine> orderLines = order.getOrderLines();
        InventoryReservation inventoryReservation = new InventoryReservation();
        for (OrderLine ol : orderLines) {
            inventoryReservation.addInvResLine(ol.getProductId(), ol.getQuantity());
        }
        if (!getInventoryClientBean().reserveInventory(inventoryReservation, authHeader)) {
            logger.error("Unable to reserve inventory; context={}", inventoryReservation);
            return false;
        }
        return true;
    }

    public static String createOrderId() {
        return String.valueOf(UUID.randomUUID());
    }

    public boolean removeOrders() {
        return getOrderDaoBean().removeOrders();
    }
}
