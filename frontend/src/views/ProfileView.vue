<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

const profileImage = ref("/favicon.ico"); //faire une pdp de base comme discord
const username = ref("");
const email = ref("");
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const token = localStorage.getItem("token");
const userUUID = localStorage.getItem("userUUID");


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


const changeUsername = async () => {

    if (!userUUID) {
        return;
    }

    const usernameChanges = {
        username: username.value,
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userUUID}/username`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(usernameChanges)
        });

        if (!response.ok) {
            return;
        }

        alert("Pseudo modifié !")

    } catch (error) {
        console.log(error);
    }
};

const changeEmail = async () => {

    if (!userUUID) {
        return;
    }

    const emailChanges = {
        email: email.value,
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userUUID}/email`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(emailChanges)
        });

        if (!response.ok) {
            return;
        }

        alert("Email modifié !")

    } catch (error) {
        console.log(error);
    }
};


const changePassword = async () => {

    if (!userUUID) {
        return;
    }

    if (newPassword.value !== confirmPassword.value) {
        alert("Les mots de passe ne correspondent pas !");
        return;
    }

    const passwordChannges = {
        oldPassword: oldPassword.value,
        newPassword: newPassword.value,
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userUUID}/password`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(passwordChannges)
        });

        if (!response.ok) {
            return;
        }

        alert("Mot de passe modifié !")

    } catch (error) {
        console.log(error);
    }

};


const deleteAccount = async () => {

    if (!userUUID) {
        return;
    }

    const confirmation = confirm("Êtes-vous sûr à 100% de vouloir supprimer votre compte ?");
    if (!confirmation) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/users/${userUUID}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            return;
        }

        alert("Compte Supprimé !")
        localStorage.removeItem("token");
        localStorage.removeItem("userUUID");
        router.push('/login');

    } catch (error) {
        console.log(error);
    }
};

// Rajouter de quoi voir les infos actuelles de l'utilisateur
</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">Profil de {{ username }}</h1>

        <div class="flex flex-col items-center mb-6">
            <img :src="profileImage"
                class="w-24 h-24 rounded-full border-2 border-gray-700 hover:border-white transition">
            <input type="file" @change="changePfp" class="mt-3 text-sm text-gray-400">
        </div>


        <div class="w-full max-w-md mb-4">
            <label class="block text-gray-300 mb-1">Changer de username</label>
            <input v-model="username" type="email" placeholder="Nouveau username"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <button @click="changeUsername"
                class="mt-2 w-full bg-blue-500 hover:bg-blue-600 text-white py-2 rounded-lg">Enregistrer</button>
        </div>


        <div class="w-full max-w-md mb-4">
            <label class="block text-gray-300 mb-1">Changer de Email</label>
            <input v-model="email" type="text" placeholder="Nouvel Email"
                class="w-full px-4 py-2 bg-gray-800 border border-gray-600 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <button @click="changeEmail"
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
                class="mt-4 w-full bg-purple-500 hover:bg-purple-600 text-white py-2 rounded-lg">Changer le mot de
                passe</button>
        </div>

        <div class="w-full max-w-md mb-4">
            <button @click="deleteAccount"
                class="mt-4 w-full bg-red-500 hover:bg-red-600 text-white py-2 rounded-lg">Supprimer le compte</button>
        </div>

    </div>
</template>
