<script setup>
import { ref, onMounted } from 'vue';

const friendRequests = ref([]);
const guildInvites = ref([]);
const errorMessage = ref("");
const token = localStorage.getItem("token");

if (!token) {
  router.push("/login");
}

///////////////////// FRIENDS //////////////////////

const fetchFriendRequests = async () => {

  try {
    const response = await fetch(`http://localhost:8080/friends/requests`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      return;
    }

    friendRequests.value = await response.json();
    console.log(friendRequests.value);

  } catch (error) {
    errorMessage.value = "Erreur lors du chargement des requetes d'amis" + error;
  }
};


const acceptFriendRequest = async (friend_id) => {

  if (!friend_id) {
    return;
  }

  // On protege les routes
  // seul un utilisateur connecté peut chercher des amis
  // donc on utilise son token
  try {
    const response = await fetch(`http://localhost:8080/friends/accept`, {
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

    // actualiser pour faire disparaitre la demande
    fetchFriendRequests();

  } catch (error) {
    errorMessage.value = "Erreur lors acceptation de la demande d'ami" + error;
  }
};

const declineFriendRequest = async (friend_id) => {

  if (!friend_id) {
    return;
  }

  // On protege les routes
  // seul un utilisateur connecté peut chercher des amis
  // donc on utilise son token
  try {
    const response = await fetch(`http://localhost:8080/friends/decline`, {
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

    // actualiser pour faire disparaitre la demande
    fetchFriendRequests();

  } catch (error) {
    errorMessage.value = "Erreur lors du refus de la demande d'ami " + error;
  }

};

///////////////////// GUILDS //////////////////////

const fetchGuildInvites = async () => {
  try {
    const response = await fetch(`http://localhost:8080/guilds/invites`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
    });
    if (!response.ok) {
      return;
    }

    guildInvites.value = await response.json();
    console.log(guildInvites.value);

  } catch (error) {
    console.log(error);
  }
};

const acceptGuildInvite = async (guild_id) => {

  if (!guild_id) {
    return;
  }

  try {
    const response = await fetch(`http://localhost:8080/guilds/invites/accept`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({guild_id: guild_id})
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchGuildInvites();

  } catch (error) {
    errorMessage.value = "Erreur lors de l'acceptaion pour rejoindre un serveur" + error;
  }
}

const declineGuildInvite = async (guild_id) => {

  if (!guild_id) {
    return;
  }

  // On protege les routes
  // seul un utilisateur connecté peut chercher des amis
  // donc on utilise son token
  try {
    const response = await fetch(`http://localhost:8080/guilds/invites/decline`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({guild_id: guild_id})
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchGuildInvites();

  } catch (error) {
    errorMessage.value = "Erreur lors du refus de l'invitation à un serveur " + error;
  }
}

onMounted(() => {
  fetchGuildInvites();
  fetchFriendRequests();
});

</script>

<template>
  <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white space-y-16 p-8 overflow-auto">
    <div v-if="errorMessage" class="text-red-500">{{ errorMessage }}</div>

    <!-- Amis -->
    <div class="flex flex-col items-center space-y-4 w-full">
      <h1 class="text-4xl font-bold">Demandes d'amis</h1>
      <p class="text-lg text-gray-400">Demandes d'amis en attente</p>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-8 w-full max-w-4xl">
        <div v-for="request in friendRequests" :key="request.user_id"
          class="bg-gray-800 p-6 rounded-lg flex justify-between items-center">
          <span class="text-lg font-bold">{{ request.username }}</span>
          <div class="flex flex-col space-y-2">
            <button @click="acceptFriendRequest(request.user_id)"
              class="px-3 py-1 bg-green-500 hover:bg-green-600 text-white text-sm font-semibold rounded-lg">
              Accepter
            </button>
            <button @click="declineFriendRequest(request.user_id)"
              class="px-3 py-1 bg-red-500 hover:bg-red-600 text-white text-sm font-semibold rounded-lg">
              Refuser
            </button>
          </div>
        </div>
        <div v-if="friendRequests.length === 0" class="text-gray-400 text-center p-2 col-span-full">
          Aucune demande d'ami en attente
        </div>
      </div>
    </div>

    <!-- Serveurs -->
    <div class="flex flex-col items-center space-y-4 w-full">
      <h1 class="text-4xl font-bold">Rejoindre Serveur</h1>
      <p class="text-lg text-gray-400">Invitations aux serveurs en attente</p>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-8 w-full max-w-4xl">
        <div v-for="invite in guildInvites" :key="invite.guild_id"
          class="bg-gray-800 p-6 rounded-lg flex justify-between items-center">
          <span class="text-lg font-bold">{{ invite.guild_name }}</span>
          <div class="flex flex-col space-y-2">
            <button @click="acceptGuildInvite(invite.guild_id)"
              class="px-3 py-1 bg-green-500 hover:bg-green-600 text-white text-sm font-semibold rounded-lg">
              Accepter
            </button>
            <button @click="declineGuildInvite(invite.guild_id)"
              class="px-3 py-1 bg-red-500 hover:bg-red-600 text-white text-sm font-semibold rounded-lg">
              Refuser
            </button>
          </div>
        </div>
        <div v-if="guildInvites.length === 0" class="text-gray-400 text-center p-2 col-span-full">
          Aucun groupe ne veut de vous
        </div>
      </div>
    </div>
  </div>
</template>
