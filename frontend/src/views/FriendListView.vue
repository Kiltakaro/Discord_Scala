<script setup>
import { ref, onMounted } from 'vue';

const friends = ref([]);
const token = localStorage.getItem("token");
const userUUID = localStorage.getItem("userUUID");


const fetchFriends = async () => {
    try {
        const response = await fetch(`http://localhost:8080/friends/${userUUID}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
        },
    });


        if (!response.ok) {
            return;
        }

        friends.value = await response.json();
        console.log("Friends :", friends.value);

    } catch (error) {
        console.log(error);
    }
};

onMounted(() => {
    fetchFriends();
});
</script>


<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
        <h1 class="text-4xl font-bold">Liste d'amis</h1>
        <p class="text-lg text-gray-400">Voici vos Amis</p>

        <ul class="w-full max-w-md bg-gray-600 rounded-lg p-4 mt-4">
            <li v-for="friend in friends" :key="friend.userUUID"
                class="flex justify-between items-center p-2 border-b border-gray-700">
                <span>{{ friend.username }}</span>
            </li>
            <li v-if="friends.length === 0" class="text-gray-400 text-center p-2">Aucune ami, peut-être qu'un jour t'en auras
            </li>
        </ul>
    </div>
</template>