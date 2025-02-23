<script setup>
import { RouterLink } from 'vue-router';
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
    <h1 class="text-4xl font-bold">Bienvenue sur mini-discord</h1>
    <p class="text-lg text-gray-400">Une plateforme Discord-like.</p>

    <div class="flex space-x-4">
      <RouterLink to="/login" class="px-6 py-2 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
        Connexion
      </RouterLink>
      <RouterLink to="/register" class="px-6 py-2 bg-purple-500 hover:bg-purple-600 text-white font-semibold rounded-lg">
        S'inscrire
      </RouterLink>
    </div>
  </div>
</template>
