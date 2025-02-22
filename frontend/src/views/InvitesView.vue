<script setup>
import { ref, onMounted } from 'vue';

const friendRequests = ref([]);
const guildInvites = ref([]);
const token = localStorage.getItem("token");
const userUUID = localStorage.getItem("userUUID");


///////////////////// FRIENDS //////////////////////

const fetchFriendRequests = async () => {

  if (!userUUID) {
    return;
  }

  try {
    const response = await fetch(`http://localhost:8080/friends/requests/${userUUID}`, {
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
    console.log(error);
  }
};


const acceptFriendRequest = async (friendUUID) => {

  if (!userUUID) {
    return;
  }
  if (!friendUUID) {
    return;
  }

  // cf message dans le backend, on pourrait inverser les deux
  const friendInput = {
    userUUID: userUUID,
    friendUUID: friendUUID
  };

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
      body: JSON.stringify(friendInput)
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchFriendRequests();

  } catch (error) {
    console.log(error);
  }
};

const declineFriendRequest = async (friendUUID) => {

  if (!userUUID) {
    return;
  }
  if (!friendUUID) {
    return;
  }

  // cf message dans le backend, on pourrait inverser les deux
  const friendInput = {
    userUUID: userUUID,
    friendUUID: friendUUID
  };

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
      body: JSON.stringify(friendInput)
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchFriendRequests();

  } catch (error) {
    console.log(error);
  }

};

///////////////////// GUILDS //////////////////////

const fetchGuildInvites = async () => {
  try {
    const response = await fetch(`http://localhost:8080/guilds/invites/${userUUID}`, {
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

const acceptGuildInvite = async (guildUUID) => {

  if (!userUUID) {
    return;
  }
  if (!guildUUID) {
    return;
  }

  const guildInput = {
    userUUID: userUUID,
    guildUUID: guildUUID
  };


  try {
    const response = await fetch(`http://localhost:8080/guilds/invites/accept`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify(guildInput)
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchGuildInvites();

  } catch (error) {
    console.log(error);
  }
}

const declineGuildInvite = async (guildUUID) => {

  if (!userUUID) {
    return;
  }
  if (!guildUUID) {
    return;
  }

  // cf message dans le backend, on pourrait inverser les deux
  const guildInput = {
    userUUID: userUUID,
    guildUUID: guildUUID
  };

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
      body: JSON.stringify(guildInput)
    });

    if (!response.ok) {
      return;
    }

    // actualiser pour faire disparaitre la demande
    fetchGuildInvites();

  } catch (error) {
    console.log(error);
  }
}

onMounted(() => {
  fetchGuildInvites();
  fetchFriendRequests();
});

</script>

<template>
  <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white space-y-16 p-8 overflow-auto">
    
    <!-- Amis -->
    <div class="flex flex-col items-center space-y-4 w-full">
      <h1 class="text-4xl font-bold">Demandes d'amis</h1>
      <p class="text-lg text-gray-400">Demandes d'amis en attente</p>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-8 w-full max-w-4xl">
        <div v-for="request in friendRequests" :key="request.userUUID"
          class="bg-gray-800 p-6 rounded-lg flex justify-between items-center">
          <span class="text-lg font-bold">{{ request.username }}</span>
          <div class="flex flex-col space-y-2">
            <button @click="acceptFriendRequest(request.userUUID)"
              class="px-3 py-1 bg-green-500 hover:bg-green-600 text-white text-sm font-semibold rounded-lg">
              Accepter
            </button>
            <button @click="declineFriendRequest(request.userUUID)"
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
        <div v-for="invite in guildInvites" :key="invite.guildUUID"
          class="bg-gray-800 p-6 rounded-lg flex justify-between items-center">
          <span class="text-lg font-bold">{{ invite.guild_name }}</span>
          <div class="flex flex-col space-y-2">
            <button @click="acceptGuildInvite(invite.guildUUID)"
              class="px-3 py-1 bg-green-500 hover:bg-green-600 text-white text-sm font-semibold rounded-lg">
              Accepter
            </button>
            <button @click="declineGuildInvite(invite.guildUUID)"
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
