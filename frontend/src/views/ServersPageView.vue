<script setup>
import { ref, onMounted } from 'vue';

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


const fetchGuilds = async () => {
    try {
        const response = await fetch(`http://localhost:8080/users/guilds/${userUUID}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
        },
    });

        if (!response.ok) {
            return;
        }

        guilds.value = await response.json();
        console.log("Guilds :", guilds.value);

    } catch (error) {
        console.log(error);
    }
};


//Je change des qu'on a les pages de chat
const friendRedirect = (friend) => {
    console.log(`redirect to ${friend.username}`);
};

const guildRedirect = (server) => {
    console.log(`redirect to ${guilds.name}`);
};

onMounted(() => {
    fetchFriends();
    fetchGuilds();
});
</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">Salons Mini-Discord</h1>

        <div class="w-full max-w-lg mb-8">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Messages Privés</h2>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="friend in friends" :key="friend.userUUID" @click="friendRedirect(friend)"
                    class="flex flex-col items-center focus:outline-none">
                    <img :src="messages.icon"
                        class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition">
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ friend.username }}</span>
                </button>
            </div>
        </div>

        <div class="w-full max-w-lg">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Serveurs</h2>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="guild in guild" :key="guild.id" @click="guildRedirect(guild)"
                    class="flex flex-col items-center focus:outline-none">
                    <img :src="server.icon"
                        class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition">
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ guild.name }}</span>
                </button>
                <RouterLink to="/create-guild" class="mt-4 bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded-lg">
                    Créer un serveur
                </RouterLink>
            </div>
        </div>
    </div>
</template>
