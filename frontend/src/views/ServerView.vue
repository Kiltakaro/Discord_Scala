<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const guildId = ref(route.params.id);
const guild = ref(null);
const errorMessage = ref(null);
const token = localStorage.getItem("token");

const fetchGuild = async () => {
    try {
        const response = await fetch(`http://localhost:8080/guilds/${guildId.value}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const data = await response.json();
        console.log("Guild data :", data);

        if (data.guild_name) {
            guild.value = data;
        } else {
            errorMessage.value = "Aucune donnée reçue";
        }
    } catch (error) {
        errorMessage.value = "Erreur de chargement du serveur";
    }
};

onMounted(() => {
    fetchGuild();
});
</script>

<template>
  <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
    <h1 class="text-3xl font-bold mb-6">
      Serveur: {{ guild?.guild_name || "Chargement..." }}
    </h1>
    <p>{{ guild?.guild_desc || "Aucune description disponible" }}</p>
    <p>{{ guild?.owner_id || "Propriétaire inconnu" }}</p>
    <p v-if="errorMessage" class="text-red-500">{{ errorMessage }}</p>
  </div>
</template>
