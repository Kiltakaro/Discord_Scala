<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const friends = ref([]);
const token = localStorage.getItem("token");
const errorMessage = ref("");
const router = useRouter();

if (!token) {
  router.push("/login");
}

const fetchFriends = async () => {
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
        errorMessage.value = "Erreur lors du chargement des amis " + error;
    }
};

const userPrivateMessagesRedirect = (user) => {
    router.push(`/friends/${user.user_id}`);
};

onMounted(() => {
    fetchFriends();
});
</script>


<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
        <h1 class="text-4xl font-bold">Liste d'amis</h1>
        <p class="text-lg text-gray-400">Voici vos Amis</p>
        <div v-if="errorMessage" class="text-red-500">{{ errorMessage }}</div>


        <div class="w-full max-w-lg mb-8">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Messages Privés</h2>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="friend in friends" :key="friend.user_id" @click="userPrivateMessagesRedirect(friend)"
                    class="flex flex-col items-center focus:outline-none">
                    <IconMessage class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition" />
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ friend.username }}</span>
                </button>
            </div>
        </div>

        <router-link to="/searchfriend" class="hover:text-blue-400 font-bold transition">
            <button class="mt-4 w-full bg-blue-500 hover:bg-blue-600 text-white py-3 px-6 rounded-lg">Ajouter des
                amis</button> 
        </router-link>

    </div>

</template>