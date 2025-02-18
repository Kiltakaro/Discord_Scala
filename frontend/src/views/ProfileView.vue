<script setup>
import { ref } from "vue";

const profileImage = ref("/favicon.ico"); //faire une pdp de base comme discord
const pseudo = ref("kiffeur2Scala");
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");


//A supprimer si on décide de ne pas faire de pdp custom, ça dépend de la solution pour l'hébergement
const changePfp = (event) => {
    const file = event.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            profileImage.value = e.target.result;
        };
        reader.readAsDataURL(file);
    }
};


//console log juste pour check l'appel des fonctions
const changeName = () => {
    console.log(pseudo.value);
};

//rajouter vérif entre les mots de passe 
const changePassword = () => {
    console.log("ancien mdp :", oldPassword.value);
    console.log("nouveau mdp :", newPassword.value);
    console.log("confirmation mdp :", confirmPassword.value);
};
</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">Profil de {{ pseudo }}</h1>

        <div class="flex flex-col items-center mb-6">
            <img :src="profileImage"
                class="w-24 h-24 rounded-full border-2 border-gray-700 hover:border-white transition">
            <input type="file" @change="changePfp" class="mt-3 text-sm text-gray-400">
        </div>


        <div class="w-full max-w-md mb-4">
            <label class="block text-gray-300 mb-1">Changer de pseudo</label>
            <input v-model="pseudo" type="text" placeholder="Nouveau pseudo"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <button @click="changeName"
                class="mt-2 w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg">Enregistrer</button>
        </div>

        <div class="w-full max-w-md mb-4">
            <label class="block text-gray-300 mb-1">Ancien mot de passe</label>
            <input v-model="oldPassword" type="password" placeholder="Ancien mot de passe"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

            <label class="block text-gray-300 mt-4 mb-1">Nouveau mot de passe</label>
            <input v-model="newPassword" type="password" placeholder="Nouveau mot de passe"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

            <label class="block text-gray-300 mt-4 mb-1">Confirmer le mot de passe</label>
            <input v-model="confirmPassword" type="password" placeholder="Confirmer le mot de passe"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">

            <button @click="changePassword"
                class="mt-4 w-full bg-purple-500 hover:bg-purple-600 text-white py-2 rounded-lg">Changer le mot de passe</button>
        </div>
    </div>
</template>
