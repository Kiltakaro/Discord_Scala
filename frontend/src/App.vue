<script setup>
import {RouterView } from 'vue-router'
import Navbar from './views/NavBarView.vue'
</script>

<template>
  <Navbar />
  <RouterView />
</template>


<script>
export default {
  name: 'App',
};
</script>

<style scoped>
</style>
