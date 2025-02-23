<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const guildName = ref("");
const guildDescription = ref("");
const errorMessage = ref("");
const userUUID = localStorage.getItem("userUUID");
const token = localStorage.getItem("token");

const createGuild = async () => {
  if (!guildName.value.trim()) {
    errorMessage.value = "Le serveur doit avoir un nom";
    return;
  }

  try {
    const response = await fetch("http://localhost:8080/guilds/create", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({
        guildName: guildName.value,
        guildDescription: guildDescription.value,
        ownerId: userUUID
      })
    });

    if (response.ok) {
      const data = await response.json();
      alert("Serveur créé");
      router.push(`/server/${data.guildId}`); // Redirige vers le nouveau serveur
    } else {
      const errorData = await response.json();
      errorMessage.value = errorData.error || "Erreur lors de la création du serveur.";
    }
  } catch (error) {
    console.error(error);
    errorMessage.value = "Une erreur s'est produite. Veuillez réessayer.";
  }
};
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white p-6">
    <h1 class="text-3xl font-bold mb-6">Créer un serveur</h1>

    <div v-if="errorMessage" class="text-red-500">{{ errorMessage }}</div>

    <div class="w-full max-w-md">
      <label class="block text-gray-300 mb-1">Nom du serveur</label>
      <input v-model="guildName" type="text" placeholder="Nom du serveur"
        class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

      <label class="block text-gray-300 mt-4 mb-1">Description</label>
      <input v-model="guildDescription" type="text" placeholder="Description (optionnel)"
        class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

      <button @click="createGuild"
        class="mt-4 w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg">Créer</button>
    </div>
  </div>
</template>
