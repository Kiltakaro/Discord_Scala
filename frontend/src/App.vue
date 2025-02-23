<script setup>
import {RouterView } from 'vue-router'
import Navbar from './views/NavBarView.vue'
</script>

<template>
  <Navbar />
  <!-- Pour eviter que la navbar se superpose avec des composants -->
  <div class="pt-16">
    <RouterView />
  </div>
</template>


<script>
export default {
  name: 'App',
};
</script>

<style scoped>
</style>
