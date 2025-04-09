<script setup>
// tuto context menu (goatesque): https://medium.com/@sj.anyway/custom-right-click-context-menu-in-vue3-b323a3913684
import { ref, onMounted } from 'vue';
import {useRoute, useRouter} from 'vue-router';
import MenuView from "@/views/MenuView.vue";

const router = useRouter();
const route = useRoute();
const guildId = ref(route.params.id);
const guild = ref(null);
const errorMessage = ref(null);
const token = localStorage.getItem("token");
const user_id = localStorage.getItem("user_id");
const owner = ref(false);
const users = ref([]);

const channels_in_guild = ref([]);
// pour les invitations
const username = ref('');
const users_in_guild = ref([]);

// Context menu variables
// ON SE SERT DE ÇA LE + POSSIBLE SI ON PEUT, ÇA ÉVITE DE SPAM LES BOUTONS PARTOUT
const showMenuChannel = ref(false);
const showMenuUser = ref(false);
const targetChannelId = ref(""); // Utilisé pour déterminer sur quel channel on a fait clic droit
const targetUserId = ref(""); // Sur quel user on a fait clic droit
const menuX = ref(0);
const menuY = ref(0);
const contextMenuActionsChannel = ref([
    { label: 'Supprimer', action: 'delete'}
]);

const contextMenuActionsUser = ref([
    { label: 'Expulser', action: 'kick' },
    { label: 'Bannir', action: 'ban' }
]);


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
    if (!invited_id) {
        return;
    }

    // On protege les routes
    // seul un utilisateur connecté peut chercher des amis
    // donc on utilise son token
    try {
        // On n'envoie pas d'invitation à un user banni
        const checkBanResponse = await fetch(`http://localhost:8080/guilds/${guildId.value}/ban/${invited_id}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
        });

        if (!checkBanResponse.ok) {
            alert("Impossible d'envoyer une invitation à cet utilisateur");
            throw new Error(`HTTP Error : ${checkBanResponse.status}`)
        }

        const inviteResponse = await fetch(`http://localhost:8080/guilds/invites/add`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({invited_id: invited_id, guild_id: guildId.value})
        });

        if (!inviteResponse.ok) {
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

const fetchChannelsInGuild = async() => {
    try {
        const response = await fetch(`http://localhost:8080/channels/${guildId.value}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        channels_in_guild.value = await response.json();
    } catch (error) {
        errorMessage.value = `Erreur lors de la récupération des channels : ${error}`;
    }
};

const deleteChannel = async(channelId) => {
    try {
        const response = await fetch(`http://localhost:8080/channels/${guildId.value}/${channelId}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        await fetchChannelsInGuild();
    } catch (error) {
        errorMessage.value = `Erreur lors de la suppression du channel : ${error}`;
    }
}

const getChannelMessages = async (channelId) => {
    // TODO
}

const banUser = async (banned_id) => {

    if (!banned_id) {
        return;
    }

    const confirmation = confirm("Êtes-vous sûr à 100% de vouloir bannir cet utilisateur ?");
    if (!confirmation) {
        return;
    }

    const banInput = {
        user_id: banned_id,
        guild_id: guildId.value
    };
    // Sur la route va falloir verifier que l'utilisateur est bien l'admin

    try {
        const response = await fetch(`http://localhost:8080/guilds/ban`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(banInput)
        });

        if (!response.ok) {
            throw new Error(`Erreur : ${response.status}`);
        }
        console.log("Banned id :", banned_id);
        alert("L'utilisateur a bien été banni du serveur");
        await fetchUsersInGuild();

    } catch (error) {
        console.log(error);
    }
}

const kickUser = async (kicked_id) => {

    // Sur la route va falloir verifier que l'utilisateur est bien l'admin

    if (!kicked_id) {
        return;
    }

    // rajouter un truc qui affiche le nom du user, peut etre
    const confirmation = confirm("Êtes-vous sûr à 100% de vouloir l'expulser ?");
    if (!confirmation) {
        return;
    }

    try {

        const response = await fetch(`http://localhost:8080/guilds/${guildId.value}/kick/${kicked_id}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            return;
        }

        alert("Utilisateur expulsé du serveur");
        fetchUsersInGuild();

    } catch (error) {
        console.log(error);
    }
}

const deleteGuild = async () => {
    const confirmation = confirm("Êtes-vous sûr de vouloir supprimer ce serveur ?");
    if (!confirmation) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/guilds/${guildId.value}`, {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (response.status === 403) {
            alert("Vous n'êtes pas propriétaire de ce serveur");
            return;
        }
        if (!response.ok) {
            throw new Error("Failed to delete the server");
        }

        alert("Serveur supprimé");
        router.push("/serverList");
    } catch (error) {
        console.error(error);
        router.push("/serverList");
    }
};

// Affiche un menu en faisant clic droit sur un channel
const displayMenuChannel = (event, channelId) => {
    event.preventDefault();
    showMenuChannel.value = true;
    targetChannelId.value = channelId;
    menuX.value = event.clientX;
    menuY.value = event.clientY;
};

const displayMenuUser = (event, userId) => {
    if(user_id === userId) return;

    event.preventDefault();
    showMenuUser.value = true;
    targetUserId.value = userId;
    menuX.value = event.clientX;
    menuY.value = event.clientY;
}

// Permet de fermer le menu (pas entièrement fonctionnel pour le moment)
const closeMenu = () => {
    showMenuChannel.value = false;
    showMenuUser.value = false;
};

// Ici on peut éventuellement gérer d'autres actions genre edit, etc
const handleMenuActionsChannel = (action) => {
    if(action === 'delete') {
        deleteChannel(targetChannelId.value);
    }
    closeMenu();
};

const handleMenuActionsUser = (action) => {
    switch (action) {
        case 'kick':
            kickUser(targetUserId.value);
            break;

        case 'ban':
            banUser(targetUserId.value);
            break;
    }
    closeMenu();
}

const banListRedirect = async () => {
    await router.push(`/server/${guildId.value}/bans`);
};

const createChannelRedirect = async () => {
    await router.push(`/server/${guildId.value}/create-channel`);
};

onMounted(() => {
    fetchGuild();
    fetchUsersInGuild();
    fetchChannelsInGuild();
});
</script>

<template>
    <div class="pr-64">
        <div class="fixed bg-transparent w-full h-full" @click="closeMenu" v-if="showMenuUser || showMenuChannel"></div>
        <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
            <h1 class="text-3xl font-bold mb-6">
                Serveur: {{ guild?.guild_name || "Chargement..." }}
            </h1>
            <p>{{ guild?.guild_desc || "Aucune description disponible" }}</p>
            <p v-if="errorMessage" class="text-red-500">{{ errorMessage }}</p>
            <p> Voici un texte hyper long pour tester si le padding right marche bien et si la barre d'utilisateurs ne
                va pas passer par dessus le texte et le rendre illisible psk ça serait vraiment dommage de pas pouvoir
                observer un tel message</p>


            <!-- Ajouter des utilisateurs (Réservé a l'admin du serveur) -->
            <!-- Faudra trouver un moyen plus stylé de faire ça -->
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
                        <div v-if="users.length === 0" class="text-gray-400 text-center p-2 col-span-full">Aucun
                            utilisateur
                            trouvé</div>
                    </div>
                </div>
            </div>

            <!-- Liste des channels -->
            <div
                class="fixed left-0 top-2 bottom-0 w-64 bg-gray-800 p-4 border-l-4 border-gray-700 overflow-y-auto mt-16">
                <h2 class="text-xl font-bold mb-4">Channels</h2>
                <ul>
                    <li v-for="channel in channels_in_guild" :key="channel.id"
                        class="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-gray-700"
                        @click="getChannelMessages(channel.id)"
                        @contextmenu.prevent="displayMenuChannel($event, channel.id)">
                    {{channel.name}}
                    </li>
                </ul>
                <button v-if="owner" @click="createChannelRedirect()"
                    class="ml-auto px-4 py-1 bg-purple-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
                    Nouveau channel
                </button>

            <!-- affichage du menu clic droit pour les channels -->
            <MenuView
                v-if="showMenuChannel && owner"
                :actions="contextMenuActionsChannel"
                @action-clicked="handleMenuActionsChannel"
                :x="menuX"
                :y="menuY"
            />
            </div>

            <!-- Liste des membres du serveur -->
            <div
                class="fixed right-0 top-2 bottom-0 w-64 bg-gray-800 p-4 border-l-4 border-gray-700 overflow-y-auto mt-16">
                <h2 class="text-xl font-bold mb-4">Membres du serveur</h2>
                <button v-if="owner" @click="deleteGuild"
                    class="mt-4 px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-semibold rounded-lg">
                    Supprimer le serveur
                </button>
                <ul>
                    <li v-for="user in users_in_guild" :key="user.uuid"
                        class="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-gray-700"
                        @contextmenu.prevent="displayMenuUser($event, user.uuid)">
                        <span class="flex-1">{{ user.username }}</span>

                        <!-- Visible que pour l'admin + empêche l'admin de se ban / kick lui-même -->
                        <MenuView
                            v-if="showMenuUser && owner"
                            :actions="contextMenuActionsUser"
                            @action-clicked="handleMenuActionsUser"
                            :x="menuX"
                            :y="menuY"
                        />
                    </li>
                </ul>
                <button v-if="owner"
                    class="ml-auto px-4 py-1 bg-purple-500 hover:bg-blue-600 text-white font-semibold rounded-lg"
                    @click="banListRedirect(guildId)">
                    Utilisateurs bannis
                </button>
            </div>
        </div>
    </div>
</template>
