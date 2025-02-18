<script setup>
import { ref } from "vue";


//serveurs et mp de test
const servers = ref([
    { id: 1, name: "Serveur 1", icon: "/favicon.ico" },
    { id: 2, name: "Serveur 2", icon: "/favicon.ico" },
    { id: 3, name: "Serveur 3", icon: "/favicon.ico" },
    { id: 4, name: "Serveur super genial", icon: "/favicon.ico" },
    { id: 5, name: "Serveur avec un nom tres long pour voir si le truc explose pas", icon: "/favicon.ico" },
    { id: 6, name: "👍", icon: "/favicon.ico" },
]);

//Id temporaires mais a changer en fonction de si un mp = un serv pour la bdd

const privateMessages = ref([
    { id: 1, name: "Brice", icon: "/favicon.ico" },
    { id: 2, name: "Yani", icon: "/favicon.ico" },
    { id: 3, name: "Hugo", icon: "/favicon.ico" },
    { id: 4, name: "Loan", icon: "/favicon.ico" },
    { id: 5, name: "JB la malice", icon: "/favicon.ico" },
    { id: 6, name: "", icon: "/favicon.ico" },
    { id: 7, name: "a", icon: "/favicon.ico" },
]);


//A changer avec une redirection vers le serv soon
const clickTest = (channel) => {
    console.log(`${channel.name}`);
};
</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">Salons Mini-Discord</h1>

        <div class="w-full max-w-lg mb-8">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Messages Privés</h2>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="messages in privateMessages" :key="messages.id" @click="clickTest(messages)"
                    class="flex flex-col items-center focus:outline-none">
                    <img :src="messages.icon"
                        class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition">
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ messages.name }}</span>
                </button>
            </div>
        </div>

        <div class="w-full max-w-lg">
            <h2 class="text-xl font-semibold mb-4 text-gray-300 text-center">Serveurs</h2>
            <div class="flex flex-wrap justify-center gap-6">
                <button v-for="server in servers" :key="server.id" @click="clickTest(server)"
                    class="flex flex-col items-center focus:outline-none">
                    <img :src="server.icon"
                        class="w-16 h-16 rounded-full border-2 border-gray-700 hover:border-white transition">
                    <span class="mt-2 text-sm text-gray-300 max-w-[80px] truncate text-center">{{ server.name }}</span>
                </button>
            </div>
        </div>
    </div>
</template>
