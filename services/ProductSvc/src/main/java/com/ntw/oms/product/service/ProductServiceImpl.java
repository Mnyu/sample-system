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

package com.ntw.oms.product.service;

import com.ntw.oms.product.dao.ProductDao;
import com.ntw.oms.product.dao.ProductDaoFactory;
import com.ntw.oms.product.entity.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

/**
 * Created by anurag on 30/05/17.
 */
@Configuration
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
// https://www.baeldung.com/spring-boot-failed-to-configure-data-source
@Component
public class ProductServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductDaoFactory productDaoFactory;

    private ProductDao productDaoBean;

    @Value("${database.type}")
    private String productDBType;

    @PostConstruct
    public void postConstruct()
    {
        this.productDaoBean = productDaoFactory.getProductDao(productDBType);
    }

    public ProductDao getProductDaoBean() {
        return productDaoBean;
    }

    public List<Product> getProducts() {
        List<Product> products = getProductDaoBean().getProducts();
        products.sort(new Comparator<Product>() {
            @Override
            public int compare(Product o1, Product o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        return products;
    }

    public Product getProduct(String id) {
        return getProductDaoBean().getProduct(id);
    }

    public boolean addProduct(Product product) {
        return getProductDaoBean().addProduct(product);
    }

    public Product modifyProduct(Product product) {
        return getProductDaoBean().modifyProduct(product);
    }

    public boolean removeProduct(String id) {
        return getProductDaoBean().removeProduct(id);
    }

    public boolean removeProducts() {
        return getProductDaoBean().removeProducts();
    }
}
