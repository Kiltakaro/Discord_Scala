<script setup>
import { ref } from 'vue';

const username = ref('');
const users = ref([]);
const token = localStorage.getItem("token");

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
    console.log(error);
  }
};

// Il va falloir rajouter un truc pour que l'on ne puisse pas spam l'invitation
const addFriend = async (friendUUID) => {

  const userUUID = localStorage.getItem("userUUID");
  console.log(userUUID);
  console.log("friend string : " + friendUUID);

  if (!userUUID) {
    console.log("No userUUID found??????????");
    return;
  }

  const friendInput = {
    userUUID: userUUID,
    friendUUID: friendUUID
  };

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
      body: JSON.stringify(friendInput)
    });

    if (!response.ok) {
      return;
    }
    alert("Demande d'ami envoyée !");

  } catch (error) {
    console.error(error);
  }
};
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
    <h1 class="text-4xl font-bold">Rechercher des amis</h1>
    <p class="text-lg text-gray-400">Ajoutez vos amis</p>

    <input v-model="username" @input="searchUsers" type="text" placeholder="Recherchez un ami"
      class="px-4 py-2 rounded-lg bg-gray-800 text-white border border-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500" />

    <ul class="w-full max-w-md bg-gray-600 rounded-lg p-4 mt-4">
      <li v-for="user in users" :key="user.uuid" class="flex justify-between items-center p-2 border-b border-gray-700">
        <span>{{ user.username }}</span>
        <button @click="addFriend(user.uuid)"
          class="px-4 py-1 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
          Ajouter
        </button>
      </li>
      <li v-if="users.length === 0" class="text-gray-400 text-center p-2">Aucun utilisateur trouvé</li>
    </ul>
  </div>
</template>
