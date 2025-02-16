<script setup>
import { ref } from 'vue';

const username = ref('');
const email = ref('');
const password = ref('');

const register = () => {
    console.log('Username:', username.value);
    console.log('Email:', email.value);
    console.log('Password:', password.value);
};

</script>

<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
        <h1 class="text-4xl font-bold">Bienvenue sur mini-discord</h1>
        <p class="text-lg text-gray-400">Une plateforme Discord-like.</p>

        <form @submit.prevent="register" class="space-y-4">
            <div>
                <label for="username" class="block text-sm font-medium text-gray-300">Nom d'utilisateur</label>
                <input v-model="username" type="text" id="username"
                    class="mt-1 block w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    required>
            </div>
            <div>
                <label for="email" class="block text-sm font-medium text-gray-300">email</label>
                <input v-model="email" type="email" id="email"
                    class="mt-1 block w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    required>
            </div>
            <div>
                <label for="password" class="block text-sm font-medium text-gray-300">Nom d'utilisateur</label>
                <input v-model="password" type="password" id="password"
                    class="mt-1 block w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500"
                    required>
            </div>
            <button type="submit"
                class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">S'inscrire</button>
        </form>
    </div>
</template>