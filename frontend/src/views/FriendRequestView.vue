<script setup>
import { ref, onMounted } from 'vue';

const friendRequests = ref([]);
const token = localStorage.getItem("token");
const userUUID = localStorage.getItem("userUUID");


const fetchFriendRequests = async () => {
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
  console.log(friendUUID);
};

const declineFriendRequest = async (friendUUID) => {
  console.log(friendUUID);
};

onMounted(() => {
  fetchFriendRequests();
});

</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
    <h1 class="text-4xl font-bold">Demandes d'amis</h1>
    <p class="text-lg text-gray-400">Demandes d'amis en attente</p>

    <ul class="w-full max-w-md bg-gray-600 rounded-lg p-4 mt-4">
      <li v-for="request in friendRequests" :key="request.userUUID" class="flex justify-between items-center p-2 border-b border-gray-700">
        <span>{{ request.username }}</span>
        <button @click="acceptFriendRequest(request.userUUID)"
          class="px-4 py-1 bg-green-500 hover:bg-green-600 text-white font-semibold rounded-lg">
          Accepter
        </button>
        <button @click="declineFriendRequest(request.userUUID)"
          class="px-4 py-1 bg-red-500 hover:bg-red-600 text-white font-semibold rounded-lg">
          Refuser
        </button>
      </li>
      <li v-if="friendRequests.length === 0" class="text-gray-400 text-center p-2">Aucune demande d'ami en attente</li>
    </ul>
  </div>
</template>