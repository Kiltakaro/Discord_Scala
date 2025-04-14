<script setup>
import { ref } from 'vue';

const username = ref('');
const users = ref([]);
const token = localStorage.getItem("token");
const errorMessage = ref("");


const searchUsers = async () => {
  // la liste des users ayant un nom similaire qui va se remplir
  if (username.value.length === 0) {
    users.value = [];
    return;
  }

  // On protege les routes
  // seul un utilisateur connecté peut chercher des amis
  // donc on utilise son token
  try {
    const response = await fetch(`http://localhost:8080/users/${username.value}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      return;
    }

    // Update les users avec les nouveaux trouvés
    users.value = await response.json();


  } catch (error) {
    errorMessage.value = "Erreur lors du chargement des utilisateurs " + error;
  }
};

// Il va falloir rajouter un truc pour que l'on ne puisse pas spam l'invitation
// soit un truc qui marche temporairement
// soit un truc qui est permanent avec une requete a l'api
const addFriend = async (friend_id) => {

  if (!friend_id) {
    return;
  }

  // On protege les routes
  // seul un utilisateur connecté peut chercher des amis
  // donc on utilise son token
  try {
    const response = await fetch(`http://localhost:8080/friends/add`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({friend_id : friend_id})
    });

    if (!response.ok) {
      return;
    }
    alert("Demande d'ami envoyée !");


  } catch (error) {
    errorMessage.value = "Erreur lors de l'ajout de l'ami " + error;
  }
};
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
    <h1 class="text-4xl font-bold">Rechercher des amis</h1>
    <p class="text-lg text-gray-400">Ajoutez vos amis</p>
    <div v-if="errorMessage" class="text-red-500">{{ errorMessage }}</div>

    <input v-model="username" @input="searchUsers" type="text" placeholder="Recherchez un ami"
      class="px-4 py-2 rounded-lg bg-gray-800 text-white border border-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500" />

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-8 w-full max-w-4xl">
      <div v-for="user in users" :key="user.uuid" class="bg-gray-800 p-6 rounded-lg shadow-md flex justify-between items-center">
        <span class="text-xl font-bold">{{ user.username }}</span>
        <button @click="addFriend(user.uuid)"
          class="ml-auto px-4 py-1 bg-purple-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
          Ajouter
        </button>
      </div>
      <div v-if="users.length === 0" class="text-gray-400 text-center p-2 col-span-full">Aucun utilisateur trouvé</div>
    </div>
  </div>
</template>
