import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ServersPageView from '../views/ServersPageView.vue'
import ServerView from '../views/ServerView.vue'
import ProfileView from '../views/ProfileView.vue'
import FriendSearchView from '../views/FriendSearchView.vue'
import InvitesView from '../views/InvitesView.vue'
import FriendListView from '../views/FriendListView.vue'
import CreateGuildView from '../views/CreateGuildView.vue'
import BanListView from "@/views/BanListView.vue";
import CreateChannelView from "@/views/CreateChannelView.vue";
import FriendMessageView from "@/views/FriendMessageView.vue";
import ManageRolesView from '@/views/ManageRolesView.vue'
import ManageGuildRolesView from '@/views/ManageGuildRoles.vue'

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
      path: '/create-guild',
      name: 'create-guild',
      component: CreateGuildView,
    },
    {
      path: '/server/:id',
      name: 'server',
      component: ServerView,
    },
    {
      path: '/server/:id/bans',
      name: 'server_bans',
      component: BanListView
    },
    {
      path: '/server/:id/create-channel',
      name: 'create_channel',
      component: CreateChannelView
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
      path: '/invites',
      name: 'invites',
      component: InvitesView,
    },
    {
      path: '/friends',
      name: 'friends',
      component: FriendListView,
    },
    {
      path: '/friends/:id',
      name: 'dmchannel',
      component: FriendMessageView
    },
    {
      path: '/server/:guildId/manage-roles/:userId',
      name: 'manageroles',
      component: ManageRolesView,
    },
    {
      path: '/server/:guildId/manage-roles',
      name: 'manageguildroles',
      component: ManageGuildRolesView,
    },
  ],
})

export default router
