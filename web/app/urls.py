from django.conf.urls import url

from app import views

urlpatterns = [
  url(r'^$', views.home),
  url(r'^loginForm$', views.login_form),
  url(r'^login$', views.login),
  url(r'^admin$', views.admin_page),
  url(r'^logout$', views.logout),
  url(r'^profile$', views.profile),
  url(r'^home$', views.home),
  url(r'^products$', views.products),
  url(r'^product$', views.product, name='views.product'),
  url(r'^cart$', views.cart, name='views.cart'),
  url(r'^createOrder$', views.create_order),
  url(r'^orders$', views.orders),
  url(r'^order$', views.order),
  url(r'^createTestData$', views.create_test_data),
  url(r'^deleteTestData$', views.delete_test_data),
  url(r'^status$', views.get_status),
  url(r'^serviceStatus$', views.get_service_status),
  url(r'^registry$', views.get_registry),
  url(r'^register$', views.register_form),
  url(r'^registerInstance$', views.register_service_instance),
  url(r'^inventoryTable$', views.get_inventory_table),
  url(r'^userAuthTable$', views.get_user_auth_table),
]
