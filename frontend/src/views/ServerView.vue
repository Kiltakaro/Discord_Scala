<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const guildId = ref(route.params.id);
const guild = ref(null);
const errorMessage = ref(null);
const token = localStorage.getItem("token");
const user_id = localStorage.getItem("user_id");
const owner = ref(false);
const users = ref([]);

// pour les invitations 
const username = ref('');
const users_in_guild = ref([]);


const searchUsers = async () => {
    // la liste des users ayant un nom similaire qui va se remplir
    if (username.value.length === 0) {
        users.value = [];
        return;
    }

    // On protege les routes
    // seul un utilisateur connecté peut chercher des amis
    // donc on utilise son token
    try {
        const response = await fetch(`http://localhost:8080/users/${username.value}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            return;
        }

        // Update les users avec les nouveaux trouvés
        users.value = await response.json();


    } catch (error) {
        console.log(error);
    }
};

// Il va falloir rajouter un truc pour que l'on ne puisse pas spam l'invitation
// soit un truc qui marche temporairement
// soit un truc qui est permanent avec une requete a l'api
const inviteUserToGuild = async (invited_id) => {

    if (!user_id) {
        return;
    }
    if (!invited_id) {
        return;
    }

    const invitedInput = {
        user_id: invited_id,
        guild_id: guildId.value,
    };

    // On protege les routes
    // seul un utilisateur connecté peut chercher des amis
    // donc on utilise son token
    try {
        const response = await fetch(`http://localhost:8080/guilds/invites/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(invitedInput)
        });

        if (!response.ok) {
            return;
        }
        alert("Demande d'ajout au serveur envoyé !");


    } catch (error) {
        console.log(error);
    }
};

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
            console.log("Données reçues");
            guild.value = data;

            if (guild.value.owner_id == user_id) {
                owner.value = true;
                console.log("propriétaire du serveur");
            }
        } else {
            errorMessage.value = "Aucune donnée reçue";
        }
    } catch (error) {
        errorMessage.value = "Erreur de chargement du serveur";
    }
};

const fetchUsersInGuild = async () => {
    try {
        const response = await fetch(`http://localhost:8080/guilds/${guildId.value}/users`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        users_in_guild.value = await response.json();
        console.log("Users :", users_in_guild.value);

    } catch (error) {
        errorMessage.value = "Erreur de chargement des utilisateurs du serveur";
    }
};

onMounted(() => {
    fetchGuild();
    fetchUsersInGuild();
});
</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h1 class="text-3xl font-bold mb-6">
            Serveur: {{ guild?.guild_name || "Chargement..." }}
        </h1>
        <p>{{ guild?.guild_desc || "Aucune description disponible" }}</p>
        <!-- <p>{{ guild?.owner_id || "Propriétaire inconnu" }}</p> -->
        <p v-if="errorMessage" class="text-red-500">{{ errorMessage }}</p>

        

        <div v-if="owner" class="flex justify-center items-center">
            <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
                <h1 class="text-4xl font-bold">Rechercher des personnes au serveur</h1>

                <input v-model="username" @input="searchUsers" type="text" placeholder="Recherchez un ami"
                    class="px-4 py-2 rounded-lg bg-gray-800 text-white border border-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500" />

                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mt-8 w-full max-w-4xl">
                    <div v-for="user in users" :key="user.uuid"
                        class="bg-gray-800 p-6 rounded-lg shadow-md flex justify-between items-center">
                        <span class="text-xl font-bold">{{ user.username }}</span>
                        <button @click="inviteUserToGuild(user.uuid)"
                            class="ml-auto px-4 py-1 bg-purple-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
                            Ajouter
                        </button>
                    </div>
                    <div v-if="users.length === 0" class="text-gray-400 text-center p-2 col-span-full">Aucun utilisateur
                        trouvé</div>
                </div>
            </div>
        </div>

        <div class="flex flex-col items-start mt-8">
            <h2 class="text-2xl font-bold mb-4">Utilisateurs dans le serveur :</h2>
            <ul>
                <li v-for="user_of_guild in users_in_guild" :key="user_of_guild.uuid">{{ user_of_guild.username }}</li>
            </ul>
        </div>
    </div>
</template>
