<script setup>
import { useRouter } from "vue-router";

const router = useRouter();
const token = localStorage.getItem("token");

const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user_id");
    router.push("/");
};

</script>

<template>
    <nav class="fixed inset-x-0 bg-gray-800 text-white p-4">
        <!-- Ne s'affiche que si on est login -->
        <div class="container mx-auto flex justify-between items-center">
            <div class="flex items-center space-x-6">
                <router-link to="/" class="text-xl font-bold text-blue-400">
                    Accueil
                </router-link>

                <div class="flex space-x-6">
                    <router-link to="/friends" class="hover:text-blue-400 font-bold transition" v-if="token">
                        Amis
                    </router-link>

                    <router-link to="/serverList" class="hover:text-blue-400 font-bold transition" v-if="token">
                        Serveurs
                    </router-link>

                    <router-link to="/profile" class="hover:text-blue-400 font-bold transition" v-if="token">
                        Profil
                    </router-link>

                    <router-link to="/invites" class="hover:text-blue-400 font-bold transition" v-if="token">
                        Invitations
                    </router-link>
                </div>
            </div>

            <div class="flex space-x-4">
                <template v-if="token">
                    <button @click="logout" class="bg-red-500 px-4 py-2 rounded-md hover:bg-red-600 transition">
                        Déconnexion
                    </button>
                </template>

                <template v-else>
                    <router-link to="/login" class="bg-blue-500 px-4 py-2 rounded-md hover:bg-blue-600 transition">
                        Connexion
                    </router-link>
                    <router-link to="/register"
                        class="bg-purple-500 px-4 py-2 rounded-md hover:bg-purple-600 transition">
                        S'inscrire
                    </router-link>
                </template>
            </div>
        </div>
    </nav>
</template>