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

package com.ntw.oms.cart.dao.sql;

import com.ntw.oms.cart.dao.CartDao;
import com.ntw.oms.cart.entity.Cart;
import com.ntw.oms.cart.entity.CartLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by anurag on 24/03/17.
 */
@Component("CartSQL")
public class DBCartLineDao implements CartDao {

    @Autowired(required = false)
    @Qualifier("cartJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DBCartLineDao.class);

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static String SQL_GET_CARTLINES = "select * from cartLine where cartId=?";

    @Override
    public Cart getCart(String id) {
        List<DBCartLine> dbCartLines;
        try {
            dbCartLines = jdbcTemplate.query(SQL_GET_CARTLINES, new Object[]{id},
                    new BeanPropertyRowMapper<>(DBCartLine.class));
        } catch (Exception e) {
            logger.error("Unable to get cart; id={}", id);
            logger.error("Exception message: ", e);
            return null;
        }
        if (dbCartLines.isEmpty()) {
            logger.debug("No cart lines found; id={}", id);
            return null;
        }
        Cart cart = DBCartLine.getCart(id, dbCartLines);
        logger.debug("Fetched cart; context={}", cart);
        return cart;
    }

    private static String SQL_INSERT_CARTLINE =
            "insert into CartLine (id, cartId, productId, quantity) values (?,?,?,?)";
    @Override
    public boolean saveCart(Cart cart) {
        List<DBCartLine> dbCartLines = DBCartLine.createDBCart(cart);
        try {
            int [] updateStatusArr = jdbcTemplate.batchUpdate(SQL_INSERT_CARTLINE, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i)
                        throws SQLException {
                    ps.setInt(1, dbCartLines.get(i).getId());
                    ps.setString(2, dbCartLines.get(i).getCartId());
                    ps.setString(3, dbCartLines.get(i).getProductId());
                    ps.setFloat(4, dbCartLines.get(i).getQuantity());
                }

                @Override
                public int getBatchSize() {
                    return dbCartLines.size();
                }
            });
            if (updateStatusArr == null) {
                logger.error("Unable to save cart; context={}", cart);
                return false;
            }
        } catch (Exception e) {
            logger.error("Unable to save cart; context={}", cart);
            logger.error("Exception message: ", e);
            return false;
        }
        logger.debug("Saved cart; context={}", cart);
        return true;
    }

    @Override
    @Transactional
    public boolean updateCart(Cart cart) {
        boolean removeCartSuccessful = removeCart(cart.getId());
        if (!removeCartSuccessful) {
            logger.warn("Cart already removed; context={}", cart);
        }
        saveCart(cart);

        logger.debug("Updated cart; context={}", cart);
        return true;
    }

    private static String SQL_DELETE_CARTLINE = "delete from cartline where cartId=?";

    @Override
    public boolean removeCart(String cartId) {
        try {
            int rowsUpdated = jdbcTemplate.update(SQL_DELETE_CARTLINE, new Object[]{cartId});
            if (rowsUpdated <= 0) {
                logger.warn("Unable to delete cart with cartId={}", cartId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Unable to remove cart with cartId={}", cartId);
            logger.error("Exception message: ", e);
            return false;
        }
        logger.debug("Removed cart; cartId={}", cartId);
        return true;
    }

    private static final String SQL_DELETE_CARTLINES = "delete from cartline";

    @Override
    public boolean removeCarts() {
        try {
            int rowsUpdated = jdbcTemplate.update(SQL_DELETE_CARTLINES);
            if (rowsUpdated < 0) {
                logger.warn("No cart lines deleted");
                return false;
            }
        } catch (Exception e) {
            logger.error("Unable to remove cart lines");
            logger.error("Exception message: ", e);
            return false;
        }
        logger.debug("Removed all cart lines");
        return true;
    }
}
