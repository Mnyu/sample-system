create database oms;

\c oms

create table Product(id varchar, name varchar, imageurl varchar, price numeric, primary key (id));
create table UserAuth(id varchar primary key, emailId varchar, name varchar, password varchar(120));
create table UserRole(id varchar, role varchar, primary key (id, role));
create table UserProfile(id varchar primary key, email varchar, name varchar);
create table UserAddress(userProfileId varchar, type varchar, id integer, street varchar, area varchar, city varchar, state varchar, country varchar, contactEmail varchar, contactName varchar, contactTelephone varchar, primary key (userProfileId, type, id)); 
create table CartLine(id integer, cartId varchar, productId varchar, quantity integer, primary key (cartId, id));
create table OrderLine(orderId varchar, orderLineId varchar, productId varchar, quantity integer, userId varchar, primary key (orderId, orderLineId));
create table Inventory(productId varchar, quantity numeric, primary key (productId));

INSERT INTO UserAuth (id, name, password, emailId) values ('admin','Admin','$2a$12$/E4.9dBmbgkHyd4Sz4WNP.eu.KCejt1.sqr7OrSjjGaow4CXmIYUi', 'admin@test.com');
INSERT INTO UserRole (id, role) VALUES ('admin','Admin');
