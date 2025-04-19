<script setup>
import { ref, onMounted } from 'vue';
import IconMessage from '@/components/icons/IconMessage.vue';
import IconServer from '@/components/icons/IconServer.vue';
import { useRouter } from "vue-router";

const router = useRouter();

const friends = ref([]);
const guilds = ref([]);
const token = localStorage.getItem("token");
const user_id = localStorage.getItem("user_id");
const errorMessage = ref(null);

if (!token) {
  router.push("/login");
}

const fetchFriends = async () => {

    if (!user_id) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/friends`, {
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
        errorMessage.value = "Erreur du changement des amis : " + error;
    }
};


const fetchGuilds = async () => {

    if (!user_id) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${user_id}/guilds`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        guilds.value = await response.json();
        console.log("Guilds :", guilds.value);

    } catch (error) {
        errorMessage.value = "Erreur lors du chargement de la liste de serveurs";
    }
};


//Je change des qu'on a les pages de chat
const friendRedirect = (friend) => {
    console.log(`redirect to ${friend.username}`);
};

// Redirection vers un serveur
const guildRedirect = async (guild_id) => {
    router.push(`/server/${guild_id}`);
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
                <button v-for="friend in friends" :key="friend.user_id" @click="friendRedirect(friend)"
                    class="flex flex-col items-center focus:outline-none">
                    <IconMessage class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition" />
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ friend.username }}</span>
                </button>
            </div>
        </div>
        <div class="w-full max-w-lg">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Serveurs</h2>
            <p v-if="errorMessage" class="text-red-500">{{ errorMessage }}</p>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="guild in guilds" :key="guild.guild_id" @click="guildRedirect(guild.guild_id)"
                    class="flex flex-col items-center focus:outline-none">
                    <IconServer class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition" />
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ guild.guild_name || "Nom de guilde"}}</span>
                </button>
            </div>
        </div>
        <RouterLink to="/create-guild" class="mt-4 bg-green-500 hover:bg-green-600 text-white py-2 px-4 rounded-lg">
                    Créer un serveur
        </RouterLink>
    </div>
</template>
