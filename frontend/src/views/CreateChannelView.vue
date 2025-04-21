<script setup>
import { ref } from "vue";
import { useRouter, useRoute} from "vue-router";

const router = useRouter();
const route = useRoute();
const channelName = ref("");
const errorMessage = ref("");
const guildId = ref(route.params.id);
const token = localStorage.getItem("token");

if (!token) {
  router.push("/login");
}

const createChannel = async () => {
    if(!channelName.value) {
        errorMessage.value("Le nom du channel ne peut pas être vide");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/channels/guilds/${guildId.value}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                name: channelName.value,
                guildId: guildId.value,
            })
        });

        if (!response.ok) {
            throw new Error(response.error);
        }
        alert("Le channel a été créé");
        await router.push(`/server/${guildId.value}`);
    } catch (error) {
        errorMessage.value = `Erreur lors de la création du channel : ${error}`;
    }
}

</script>

<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">Créer un channel</h1>

        <div v-if="errorMessage" class="text-red-500">{{ errorMessage }}</div>

        <div class="w-full max-w-md">
            <label class="block text-gray-300 mb-1">Nom du channel</label>
            <input v-model="channelName" type="text" placeholder="Nom du channel"
                   class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

            <button @click="createChannel"
                    class="mt-4 w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg">Créer</button>
        </div>
    </div>
</template>

