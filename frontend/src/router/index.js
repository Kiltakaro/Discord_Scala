import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ServersPageView from '../views/ServersPageView.vue'
import ProfileView from '../views/ProfileView.vue'
import FriendSearchView from '../views/FriendSearchView.vue'
import FriendRequestView from '../views/FriendRequestView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/serverList',
      name: 'serverList',
      component: ServersPageView,
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
    },
    {
      path: '/searchfriend',
      name: 'searchfriend',
      component: FriendSearchView,
    },
    {
      path: '/friendrequests',
      name: 'friendrequests',
      component: FriendRequestView,
    },
    
  ],
})

export default router
