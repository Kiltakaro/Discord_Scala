<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const email = ref('');
const password = ref('');


const login = async () => {

    const userInput = {
        email: email.value,
        password: password.value
    };


    try {
        const response = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userInput)
        });

        console.log(response);

        // Connexion failed
        if (!response.ok) {
            const error = await response.json();
            console.log(error);
            return;
        }

        // Connexion réussie => on garde des infos en local
        const data = await response.json();
        if (data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userUUID', data.userUUID);
            router.push('/searchfriend');
        }

    } catch (error) {
        console.log(error);
    }
};
</script>

<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
        <h1 class="text-4xl font-bold">Bienvenue sur mini-discord</h1>
        <p class="text-lg text-gray-400">Une plateforme Discord-like.</p>

        <form @submit.prevent="login" class="space-y-4">
            <div>
                <label for="email" class="block text-sm font-medium text-gray-300">Email</label>
                <input v-model="email" type="email" id="email"
                    class="mt-1 block w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    required>
            </div>
            <div>
                <label for="password" class="block text-sm font-medium text-gray-300">Mot de passe</label>
                <input v-model="password" type="password" id="password"
                    class="mt-1 block w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    required>
            </div>
            <button type="submit"
                class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">Connexion</button>
        </form>

    </div>
</template>