<script setup>
import { ref, onMounted } from 'vue';
import {useRoute, useRouter} from 'vue-router';

const router = useRouter();
const route = useRoute();
const guildId = ref(route.params.id);
const guild = ref(null);
const errorMessage = ref(null);
const token = localStorage.getItem("token");
const user_id = localStorage.getItem("user_id");
const owner = ref(false);
const banned_from_guild = ref([]);

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

            if (guild.value.owner_id === user_id) {
                owner.value = true;
                console.log("propriétaire du serveur");
            }
        } else {
            errorMessage.value = "Aucune donnée reçue";
        }
    } catch (error) {
        errorMessage.value = "Erreur de chargement du serveur : " + error;
    }
};

const fetchBannedUsers = async () => {
    try {
        const bannedUsersResponse = await fetch(`http://localhost:8080/guilds/${guildId.value}/bans`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!bannedUsersResponse.ok) {
            throw new Error(`HTTP Error : ${bannedUsersResponse.status}`);
        }

        banned_from_guild.value = await bannedUsersResponse.json();

    } catch (error) {
        errorMessage.value = "Erreur lors du chargement des utilisateurs bannis : " + error;
    }
}

const unbanUser = async (unbanId) => {
    if (!unbanId) {
        return;
    }

    const confirmation = confirm("Êtes vous sûr de vouloir débannir cet utilisateur ?");
    if (!confirmation) {
        return;
    }

    const unbanInput = {
        user_id: unbanId,
        guild_id: guildId.value
    };
    try {
        const unbanResponse = await fetch("http://localhost:8080/guilds/unban", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(unbanInput)
        });

        if (!unbanResponse.ok) {
            throw new Error(`HTTP Error : ${unbanResponse.status}`);
        }

        console.log(`unbanned id: ${unbanId}`);
        alert("L'utilisateur a été débanni du serveur");
        fetchBannedUsers();

    } catch (error) {
        console.log(error);
    }
}

const guildRedirect = async (guild_id) => {
    router.push(`/server/${guild_id}`);
};

onMounted(() => {
    fetchGuild();
    fetchBannedUsers();
});
</script>

<template>
    <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
        <h1 class="text-4xl font-bold">Liste des utilisateurs bannis</h1>

        <ul class="w-full max-w-md bg-gray-600 rounded-lg p-4 mt-4">
            <li v-for="banned_user in banned_from_guild" :key="banned_user.user_id"
                class="flex justify-between items-center p-2 border-b border-gray-700">
                <span>{{ banned_user.username }}</span>
                <button
                    v-if="owner"
                    class="mt-4 ml-4 w-full bg-green-500 hover:bg-green-600 text-white py-3 px-6 rounded-lg"
                    @click="unbanUser(banned_user.uuid)"
                >Débannir</button>
            </li>
            <li v-if="banned_from_guild.length === 0" class="text-gray-400 text-center p-2">Aucun utilisateur n'a été banni.
            </li>
        </ul>
        <div class="font-bold transition">
            <button class="mt-4 w-full bg-blue-500 hover:bg-blue-600 text-white py-3 px-6 rounded-lg"
            @click="guildRedirect(guildId)">Retour au serveur</button>
        </div>
    </div>
</template>
