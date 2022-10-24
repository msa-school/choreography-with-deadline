
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/listers/OrderCards"
import OrderDetail from "./components/listers/OrderDetail"

import ExchangeManager from "./components/listers/ExchangeCards"
import ExchangeDetail from "./components/listers/ExchangeDetail"

import PointManager from "./components/listers/PointCards"
import PointDetail from "./components/listers/PointDetail"

import DeadlineManager from "./components/listers/DeadlineCards"
import DeadlineDetail from "./components/listers/DeadlineDetail"


export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },
            {
                path: '/orders/:id',
                name: 'OrderDetail',
                component: OrderDetail
            },

            {
                path: '/exchanges',
                name: 'ExchangeManager',
                component: ExchangeManager
            },
            {
                path: '/exchanges/:id',
                name: 'ExchangeDetail',
                component: ExchangeDetail
            },

            {
                path: '/points',
                name: 'PointManager',
                component: PointManager
            },
            {
                path: '/points/:id',
                name: 'PointDetail',
                component: PointDetail
            },

            {
                path: '/deadlines',
                name: 'DeadlineManager',
                component: DeadlineManager
            },
            {
                path: '/deadlines/:id',
                name: 'DeadlineDetail',
                component: DeadlineDetail
            },



    ]
})
