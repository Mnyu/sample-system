############################################################################
# Copyright 2020 Anurag Yadav (anurag.yadav@newtechways.com)               #
#                                                                          #
# Licensed under the Apache License, Version 2.0 (the "License");          #
# you may not use this file except in compliance with the License.         #
# You may obtain a copy of the License at                                  #
#                                                                          #
#     http:#www.apache.org/licenses/LICENSE-2.0                            #
#                                                                          #
# Unless required by applicable law or agreed to in writing, software      #
# distributed under the License is distributed on an "AS IS" BASIS,        #
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
# See the License for the specific language governing permissions and      #
# limitations under the License.                                           #
############################################################################

from django.shortcuts import render, HttpResponse, HttpResponseRedirect
from django.conf import settings
from requests.exceptions import ConnectionError, HTTPError, Timeout, RequestException
from time import sleep

import requests
import json
import logging
import time
import base64

AUTH_TOKEN_COOKIE = 'auth_token'
AUTH_USER_COOKIE = 'user_auth'

ADMIN_URI = '/admin'
AUTH_TOKEN_URI = '/auth/token'
AUTH_TOKEN_USER_URI = '/auth/token/user'
AUTH_USERS_URI = '/auth/users'
PRODUCTS_URI = '/products'
CARTS_URI = '/carts'
ORDERS_URI = '/orders'
ORDER_CARTS_URI = '/orders/order/carts'
USERS_PROFILE_URI = '/users-profile'
INVENTORY_URI = '/inventory'

logger = logging.getLogger(__name__)
session = requests.session()
adapter = requests.adapters.HTTPAdapter(pool_connections=20, pool_maxsize=20)
session.mount('http://', adapter)


def get_service_address(service_id):
    logger.debug('Get service address for '+service_id)
    for trial in range(1,60):
        try:
            req = session.get(settings.REGISTRY_URL + '/' + service_id,
                               headers={'Accept':'application/json'},
                               timeout=settings.HTTP_TIMEOUT)
            if req.status_code == 200:
                application = json.loads(req.content)['application']
                for instance in application['instance']:
                    logger.debug("Hostname -> " + instance['hostName'])
                    logger.debug("Port -> " + str(instance['port']['$']))
                    logger.debug('fetched registry apps | context=%s' + json.dumps(application))
                    return instance['hostName'] + ':' + str(instance['port']['$'])
            else:
                req.raise_for_status()
        except HTTPError as http_error:
            print("Http Error:", http_error)
        except ConnectionError as conn_error:
            print("Error Connecting:", conn_error)
        except Timeout as time_error:
            print("Timeout Error:", time_error)
        except RequestException as req_error:
            print("OOps: Something went wrong. Try again.", req_error)
        logger.error('Unable to fetch service information for '+service_id+' on trial count '+str(trial))
        sleep(10)
    return ''


#REST_ENDPOINT = 'http://'+get_service_address(settings.REST_SERVICE_NAME)
REST_ENDPOINT = 'http://'+settings.REST_HOST+':'+settings.REST_PORT

logger.info('Gateway Endpoint = %s', REST_ENDPOINT)


def get_access_token(request):
    if AUTH_TOKEN_COOKIE in request.COOKIES:
        auth_token = request.COOKIES[AUTH_TOKEN_COOKIE]
        logger.debug('Extracted auth token from cookie | context=%s', auth_token)
        auth_token = json.loads(auth_token)
        return auth_token['access_token']
    return ''


def get_user(request):
    user_auth = request.COOKIES[AUTH_USER_COOKIE]
    logger.debug('Extracted UserAuth from cookie | context=%s', user_auth)
    user_auth = json.loads(user_auth)
    return user_auth['id']


def is_admin_user(request):
    if AUTH_USER_COOKIE in request.COOKIES:
        user_auth = request.COOKIES[AUTH_USER_COOKIE]
        logger.debug('Extracted UserAuth from cookie | context=%s', user_auth)
        roles = json.loads(user_auth)['roles']
        for role in roles:
            if role == 'Admin':
                return 'Admin'
    return ''


def get_product(product_id, access_token):
    req = session.get(REST_ENDPOINT + PRODUCTS_URI + '/' + product_id,
                       headers={'Authorization': 'Bearer '+access_token},
                       timeout=settings.HTTP_TIMEOUT)
    return req


def get_cart_id(request):
    return get_user(request)


def get_cart(cart_id, access_token):
    req = session.get(REST_ENDPOINT + CARTS_URI + '/' + cart_id,
                       headers={'Authorization': 'Bearer ' + access_token},
                       timeout=settings.HTTP_TIMEOUT)
    return req


def get_order(order_id, access_token):
    req = session.get(REST_ENDPOINT + ORDERS_URI + '/' + order_id,
                       headers={'Authorization': 'Bearer '+access_token},
                       timeout=settings.HTTP_TIMEOUT)
    return req


def is_product_in_cart(cart_id, product_id, access_token):
    in_cart = False
    cart_req = get_cart(cart_id, access_token)
    if cart_req.status_code == 200:
        cart = cart_req.json()
        for cart_line in cart['cartLines']:
            if product_id == cart_line['productId']:
                in_cart = True
                break
    else:
        logger.error('Unable to get cart from service | cartId=%s', cart_id)
    return in_cart


def decorate_cart(cart, access_token):
    for cart_line in cart['cartLines']:
        req = get_product(cart_line['productId'], access_token)
        if req.status_code == 200:
            cart_line['product'] = req.json()
        else:
            logger.error('Unable to get product from service | productId=%s', cart_line['productId'])
    return cart


def decorate_order(order, access_token):
    for order_line in order['orderLines']:
        req = get_product(order_line['productId'], access_token)
        if req.status_code == 200:
            order_line['product'] = req.json()
        else:
            logger.error('Unable to get product from service | productId=%s', order_line['productId'])
    return order


def decorate_orders(orders, access_token):
    for order in orders:
        decorate_order(order, access_token)
    return orders


#########################################################################
# Create your views here.
#########################################################################


def login_form(request):
    return render(request, 'app/login.html', {'message': ''})


def login(request):
    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        logger.info('Login requested | userId=%s', username)
        client_auth_str = settings.REST_CLIENT_ID + ':' + settings.REST_CLIENT_SECRET
        client_auth_bytes = client_auth_str.encode('ascii')
        client_auth_b64_bytes = base64.b64encode(client_auth_bytes)
        client_auth_b64 = client_auth_b64_bytes.decode('ascii')
        try:
            auth_token_res = session.post(REST_ENDPOINT + AUTH_TOKEN_URI,
                                           headers={'Authorization': 'Basic ' + client_auth_b64},
                                           data={'username': username, 'password': password},
                                           timeout=settings.HTTP_TIMEOUT)
            if auth_token_res.status_code == 403:
                logger.warning('Incorrect credentials for login | userId=%s', username)
                return render(request, 'app/login.html', {'message': 'Incorrect credentials'})
            auth_token_res.raise_for_status()
        except HTTPError as http_error:
            print("Http Error:", http_error)
            logger.exception("HTTPError communicating with a backend server for login | userId=%s", username)
            return render(request, 'app/login.html',
                          {'message': 'HTTP Error '+str(auth_token_res.status_code)+
                                      ' communicating with the backend server.'})
        except ConnectionError as conn_error:
            print("Error Connecting:", conn_error)
            logger.exception("Error communicating with a backend server for login | userId=%s", username)
            return render(request, 'app/login.html',
                          {'message': 'Unable to create connection with the backend server.'})
        except Timeout as time_error:
            print("Timeout Error:", time_error)
            logger.exception("Timeout communicating with a backend server for login | userId=%s", username)
            return render(request, 'app/login.html',
                          {'message': 'Timeout communicating with the backend server.'})
        except RequestException as req_error:
            print("OOps: Something went wrong. Try again.", req_error)

        if auth_token_res.status_code == 200:
            response = HttpResponseRedirect('/home')
            response.set_cookie(AUTH_TOKEN_COOKIE, auth_token_res.content.decode())
            logger.debug('User token fetched | context=%s', auth_token_res.content.decode())
            access_token = auth_token_res.json()['access_token']
            user_auth_res = session.get(REST_ENDPOINT + AUTH_TOKEN_USER_URI + '?access_token='+access_token,
                                         timeout=settings.HTTP_TIMEOUT)
            if user_auth_res.status_code == 200:
                response.set_cookie(AUTH_USER_COOKIE, user_auth_res.content.decode())
                logger.info('User logged in | context=%s', user_auth_res.content.decode())
                return response
            else:
                logger.error('Unable to fetch user auth information for user %s', username)
                logger.error('Response returned with error code %d', user_auth_res.status_code)
    return render(request, 'app/login.html', {'message': 'Login failed'})


def logout(request):
    response = HttpResponseRedirect('/loginForm')
    logger.info('User logging out | context=%s', request.META['HTTP_COOKIE'])
    response.delete_cookie(AUTH_TOKEN_COOKIE)
    response.delete_cookie(AUTH_USER_COOKIE)
    return response


def admin_page(request):
    return render(request, 'app/admin.html')


def profile(request):
    access_token = get_access_token(request)
    user_id = get_user(request)
    req = session.get(REST_ENDPOINT + USERS_PROFILE_URI +'/'+ user_id,
                       headers={'Authorization':'Bearer '+access_token},
                       timeout=settings.HTTP_TIMEOUT)
    if req.status_code == 200:
        logger.info('User profile fetched | context=%s' + req.content.decode())
        return render(request, 'app/profile.html', {'userProfile': req.json()})
    logger.error('Unable to get user profile | userId=%s', user_id)
    return HttpResponse('Unable to get user profile for ' + user_id)


def home(request):
    access_token = get_access_token(request)
    if access_token:
        return render(request, 'app/home.html', {})
    return render(request, 'app/error.html',
                  {'message': 'Please login to access this page'})


def products(request):
    access_token = get_access_token(request)
    try:
        req = session.get(REST_ENDPOINT + PRODUCTS_URI,
                           headers={'Authorization': 'Bearer '+access_token},
                           timeout=settings.HTTP_TIMEOUT)
        req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server for get products")
        return render(request, 'app/error.html',
                      {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server for get products")
        return render(request, 'app/error.html',
                      {'message': 'Unable to connect to the backend.'})
    except Timeout as time_error:
        print("Timeout Error:", time_error)
        logger.error("Timeout communicating with a backend server for get products")
        return render(request, 'app/error.html',
                      {'message': 'Timeout communicating with the backend server.'})
    except RequestException as req_error:
        print("OOps: Something went wrong. Try again.", req_error)

    if req.status_code == 200:
        logger.info('Products fetched | context=%s', req.content.decode())
        return render(request, 'app/products.html', {'products': req.json()})
    logger.error('Unable to fetch products from service | userId=%s', get_user(request))
    return HttpResponse("Unable to get products")


def product(request):
    product_id = request.GET.get('id')
    logger.debug('Get product | productId=%s', product_id)
    access_token = get_access_token(request)
    req = get_product(product_id, access_token)
    if req.status_code == 200:
        logger.info('Product fetched | context=%s', req.content.decode())
        cart_id = get_cart_id(request)
        in_cart = is_product_in_cart(cart_id, product_id, access_token)
        remove_button_visibility = 'hidden'
        if in_cart:
            remove_button_visibility = 'visible'
        return render(request, 'app/product.html',
                      {'product': req.json(),
                       'removeButtonVisibility': remove_button_visibility})
    logger.error('Unable to get product from service | productId=%s', product_id)
    return HttpResponse("Unable to get product")


def cart(request):
    access_token = get_access_token(request)
    cart_id = get_cart_id(request)
    if request.method == 'GET':
        cart_req = get_cart(cart_id, access_token)
        if cart_req.status_code == 200:
            cart = decorate_cart(cart_req.json(), access_token)
            return render(request, 'app/cart.html', {'cart': cart})
        else:
            logger.error('Unable to fetch cart | reqCode=%d | reason=%s}', cart_req.status_code, cart_req.reason)
            return HttpResponse()
    else:
        action = request.POST.get('action')
        product_id = request.POST.get('productId')
        quantity = request.POST.get('quantity')
        if action == 'remove':
            quantity = 0
        try:
            req = session.post(REST_ENDPOINT + CARTS_URI,
                                data={'id': cart_id, 'productId': product_id, 'quantity': quantity},
                                headers={'Authorization': 'Bearer ' + access_token},
                                timeout=settings.HTTP_TIMEOUT)
            req.raise_for_status()
        except HTTPError as http_error:
            print("Http Error:", http_error)
            logger.error("HTTPError communicating with the backend server for modify cart")
            return render(request, 'app/error.html',
                          {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
        except ConnectionError as conn_error:
            print("Error Connecting:", conn_error)
            logger.error("Error communicating with a backend server for modify cart")
            return render(request, 'app/error.html',
                          {'message': 'Unable to connect to the backend.'})
        except Timeout as time_error:
            print("Timeout Error:", time_error)
            logger.error("Timeout communicating with a backend server for modify cart")
            return render(request, 'app/error.html',
                          {'message': 'Timeout communicating with the backend server.'})
        except RequestException as req_error:
            print("OOps: Something went wrong. Try again.", req_error)

        if req.status_code == 201:
            logger.info('Modified cart | id=%s | productId=%s | quantity=%s', cart_id, product_id, quantity)
        else:
            logger.error('Unable to modify cart | cartId=%s', cart_id)
            return HttpResponse(status=403)
    return HttpResponse()


def orders(request):
    access_token = get_access_token(request)
    req = session.get(REST_ENDPOINT + ORDERS_URI,
                       headers={'Authorization': 'Bearer '+access_token},
                       timeout=settings.HTTP_TIMEOUT)
    if req.status_code == 200:
        logger.info('Orders fetched | context=%s', req.content.decode())
        dec_orders = decorate_orders(req.json(), access_token)
        return render(request, 'app/orders.html', {'orders': dec_orders})
    logger.error('Unable to fetch orders from service | userId=%s', get_user(request))
    return HttpResponse("Unable to get orders")


def order(request):
    order_id = request.GET.get('id')
    logger.debug('Get order | orderId=%s', order_id)
    access_token = get_access_token(request)
    req = get_order(order_id, access_token)
    if req.status_code == 200:
        logger.info('Order fetched | context=%s', req.content.decode())
        dec_order = decorate_order(req.json(), access_token)
        return render(request, 'app/order.html',
                      {'order': dec_order})
    logger.error('Unable to get order from service | orderId=%s', order_id)
    return HttpResponse("Unable to get order")


def create_order(request):
    access_token = get_access_token(request)

    cart_id = get_cart_id(request)

    req = session.post(REST_ENDPOINT + ORDER_CARTS_URI + '/'+cart_id,
                        params={}, headers={'Authorization': 'Bearer ' + access_token},
                        timeout=settings.HTTP_TIMEOUT)
    if req.status_code == 201:
        logger.info('Created order | order=%s', req.json())
    else:
        logger.error('Unable to create order | cart=%s', cart)
        return render(request, 'app/error.html',
                      {'message': 'Unable to create order due to backend server.'})
    order_dec = decorate_order(req.json(), access_token)
    return render(request, 'app/order.html', {'order': order_dec})


def create_test_data(request):
    logger.info('Creating test data')
    access_token = get_access_token(request)
    userCount = request.POST.get('userCount')
    productCount = request.POST.get('productCount')
    logger.debug('Create data | userCount=%s productCount=%s', userCount, productCount);
    if userCount and productCount:
        try:
            req = session.post(REST_ENDPOINT + ADMIN_URI + '/dataset',
                               data={'userCount': ''+userCount, 'productCount': ''+productCount},
                               headers={'Authorization': 'Bearer ' + access_token},
                               timeout=settings.HTTP_SAMPLE_DATA_TIMEOUT)
            req.raise_for_status()
        except HTTPError as http_error:
            print("Http Error:", http_error)
            logger.error("HTTPError communicating with the backend server for sample data")
            return render(request, 'app/error.html',
                          {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
        except ConnectionError as conn_error:
            print("Error Connecting:", conn_error)
            logger.error("Error communicating with a backend server for sample data")
            return render(request, 'app/error.html',
                          {'message': 'Unable to connect to the backend.'})
        except Timeout as time_error:
            print("Timeout Error:", time_error)
            logger.error("Timeout communicating with a backend server for sample data")
            return render(request, 'app/error.html',
                          {'message': 'Timeout communicating with the backend server.'})
        except RequestException as req_error:
            print("OOps: Something went wrong. Try again.", req_error)
        if req.status_code == 200:
            logger.info('Sample data created')
            return render(request, 'app/admin.html', {'message': 'Success - Created Test Data'})
        logger.error('Unable to create test data')
        return render(request, 'app/admin.html', {'message': 'Failure - Unable to create test data'})
    return render(request, 'app/admin.html', {'message': 'Specify data counts as positive integer values'})


def delete_test_data(request):
    logger.info('Deleting test data')
    access_token = get_access_token(request)
    try:
        req = session.delete(REST_ENDPOINT + ADMIN_URI + '/dataset',
                           params={}, headers={'Authorization': 'Bearer ' + access_token},
                           timeout=settings.HTTP_SAMPLE_DATA_TIMEOUT)
        req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server for sample data")
        return render(request, 'app/error.html',
                      {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server for sample data")
        return render(request, 'app/error.html',
                      {'message': 'Unable to connect to the backend.'})
    except Timeout as time_error:
        print("Timeout Error:", time_error)
        logger.error("Timeout communicating with a backend server for sample data")
        return render(request, 'app/error.html',
                      {'message': 'Timeout communicating with the backend server.'})
    except RequestException as req_error:
        print("OOps: Something went wrong. Try again.", req_error)
    if req.status_code == 200:
        logger.debug('Sample data created')
        return render(request, 'app/admin.html', {'message': 'Success - Deleted Test Data'})
    logger.error('Unable to delete test data')
    return render(request, 'app/admin.html', {'message': 'Failure - Unable to delete test data'})


def get_status(request):
    logger.debug('Get Web Status')
    my_res = 'Web Host: ' + settings.HOSTNAME + '\n'
    my_res += 'Time: ' + time.ctime(time.time()) + '\n'
    return HttpResponse(my_res)


def get_service_status(request):
    logger.debug('Get System Status')
    my_res = 'WebApp@' + settings.HOSTNAME + '\n'
    my_res += 'Time: ' + time.ctime(time.time()) + '\n\n'
    try:
        my_req = session.get(REST_ENDPOINT + ADMIN_URI + '/status',
                            timeout=settings.HTTP_TIMEOUT)
        my_req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server for get status")
        res_error = 'HTTP Error '+str(my_req.status_code)+' communicating with the backend server.'
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server for get status")
        res_error = 'Unable to connect to the backend'
    except Timeout as time_error:
        print("Timeout Error:", time_error)
        logger.error("Timeout communicating with a backend server for get status")
        res_error = 'Timeout communicating with the backend server'
    except RequestException as req_error:
        print("OOps: Something went wrong. Try again.", req_error)
        res_error = 'Unknown request error with the backend server'
    else:
        # if my_req.status_code == 200:
        my_res += my_req.content.decode()
        return render(request, 'app/admin.html',{'message': '<pre>'+my_res+'</pre>'})
    my_res = my_res + '<br/><strong>' + res_error + '</strong>'
    return render(request, 'app/admin.html', {'message': my_res})


def register_form(request):
    return render(request, 'app/register.html', {'message': ''})


def register_service_instance(request):
    host_name = request.POST.get('host')
    host_ip = request.POST.get('ip')
    port = request.POST.get('port')
    service_id = request.POST.get('id')
    service_name = request.POST.get('name')
    req_data = {
                    "instance": {
                        "hostName": host_name,
                        "app": service_name,
                        "vipAddress": service_id,
                        "secureVipAddress": service_id,
                        "ipAddr": host_ip,
                        "status": "UP",
                        "port": {"$": port, "@enabled": "true"},
                        "securePort": {"$": "8443", "@enabled": "true"},
                        "healthCheckUrl": "http://localhost:8080/status",
                        "statusPageUrl": "http://localhost:8080/status",
                        "homePageUrl": "http://localhost:8080",
                        "dataCenterInfo": {
                            "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
                            "name": "MyOwn"
                        }
                    }
                }
    req = session.post(settings.REGISTRY_URL + '/' + service_name,
                       headers={'Content-Type':'application/json'},
                       json=req_data,
                       timeout=settings.HTTP_TIMEOUT)
    if req.status_code == 204:
        logger.info('Registered new service: '+json.dumps(req_data))
        return render(request, 'app/register.html',
                      {'message': 'Success:\n<pre>'+json.dumps(req_data, indent=4)+'</pre>'})
    else:
        logger.error('Unable to register service. Returned with error code'+str(req.status_code))
        logger.error(req.content.decode())
        return render(request, 'app/register.html',
                      {'message': 'Service not Registered'+str(req.status_code)+':'+req.content.decode()})


def get_registry(request):
    logger.debug('Get Registry Status')
    try:
        req = session.get(settings.REGISTRY_URL,
                           headers={'Accept':'application/json'},
                           timeout=settings.HTTP_TIMEOUT)
        req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server")
        return render(request, 'app/error.html',
                      {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server")
        return render(request, 'app/error.html',
                      {'message': 'Unable to connect to the backend.'})

    if req.status_code == 200:
        res = json.loads(req.content)
        applications = res['applications']['application']
        my_res = ''
        for application in applications:
            my_res += '\n'+json.dumps(application, indent=4)+'\n'
        return render(request, 'app/admin.html',{'message': '<pre>'+my_res+'</pre>'})
    logger.error('Unable to get registry status')
    return HttpResponse('Unable to get registry status')


def get_inventory_table(request):
    logger.debug('Get Inventory Table')
    access_token = get_access_token(request)
    try:
        req = session.get(REST_ENDPOINT + INVENTORY_URI,
                           headers={'Authorization': 'Bearer '+access_token},
                           timeout=settings.HTTP_TIMEOUT)
        req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server for get inventory")
        return render(request, 'app/error.html',
                      {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server for get inventory")
        return render(request, 'app/error.html',
                      {'message': 'Unable to connect to the backend.'})
    except Timeout as time_error:
        print("Timeout Error:", time_error)
        logger.error("Timeout communicating with a backend server for get products")
        return render(request, 'app/error.html',
                      {'message': 'Timeout communicating with the backend server.'})
    except RequestException as req_error:
        print("OOps: Something went wrong. Try again.", req_error)

    if req.status_code == 200:
        logger.info('inventory fetched | context=%s', req.content.decode())
        return render(request, 'app/db/inventory.html', {'inventoryList': req.json()})
    logger.error('Unable to fetch inventory from service | userId=%s', get_user(request))
    return HttpResponse("Unable to get inventory")


def get_user_auth_table(request):
    logger.debug('Get User Auth Table')
    access_token = get_access_token(request)
    try:
        req = session.get(REST_ENDPOINT + AUTH_USERS_URI,
                           headers={'Authorization': 'Bearer '+access_token},
                           timeout=settings.HTTP_TIMEOUT)
        req.raise_for_status()
    except HTTPError as http_error:
        print("Http Error:", http_error)
        logger.error("HTTPError communicating with the backend server for get user auth")
        return render(request, 'app/error.html',
                      {'message': 'HTTP Error '+str(req.status_code)+' communicating with the backend server.'})
    except ConnectionError as conn_error:
        print("Error Connecting:", conn_error)
        logger.error("Error communicating with a backend server")
        return render(request, 'app/error.html',
                      {'message': 'Unable to connect to the backend.'})
    except Timeout as time_error:
        print("Timeout Error:", time_error)
        logger.error("Timeout communicating with a backend server")
        return render(request, 'app/error.html',
                      {'message': 'Timeout communicating with the backend server.'})
    except RequestException as req_error:
        print("OOps: Something went wrong. Try again.", req_error)

    if req.status_code == 200:
        logger.info('inventory fetched | context=%s', req.content.decode())
        return render(request, 'app/db/auth.html', {'authList': req.json()})
    logger.error('Unable to fetch auth data from service | userId=%s', get_user(request))
    return HttpResponse("Unable to get auth data")


