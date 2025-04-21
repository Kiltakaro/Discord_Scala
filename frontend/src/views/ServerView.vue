<script setup>
// tuto context menu (goatesque): https://medium.com/@sj.anyway/custom-right-click-context-menu-in-vue3-b323a3913684
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
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
const messages = ref([]); // c'est les msg du channel
const newMessage = ref(""); // c'est le msg que le user écrit
const actualChannel = ref(null); // channel actuel

const channels_in_guild = ref([]);
// pour les invitations
const username = ref('');
const users_in_guild = ref([]);
let refreshInterval = ref(null); // Pour le chargement des messages

// Context menu variables
// ON SE SERT DE ÇA LE + POSSIBLE SI ON PEUT, ÇA ÉVITE DE SPAM LES BOUTONS PARTOUT
const showMenuChannel = ref(false);
const showMenuUser = ref(false);
const targetChannelId = ref(""); // Utilisé pour déterminer sur quel channel on a fait clic droit
const targetUserId = ref(""); // Sur quel user on a fait clic droit
const menuX = ref(0);
const menuY = ref(0);
const contextMenuActionsChannel = ref([
    { label: 'Supprimer', action: 'delete' }
]);

const contextMenuActionsUser = ref([
    { label: 'Expulser', action: 'kick' },
    { label: 'Bannir', action: 'ban' }
]);

if (!token) {
  router.push("/login");
}

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
        errorMessage.value = "Erreur de la recherche de l'utilisateur : " + error;
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
            body: JSON.stringify({ invited_id: invited_id, guild_id: guildId.value })
        });

        if (!inviteResponse.ok) {
            return;
        }
        alert("Demande d'ajout au serveur envoyé !");


    } catch (error) {
        errorMessage.value = "Erreur de l'invitation au serveur : " + error;
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

        if (data.guild_name) {
            guild.value = data;

            if (guild.value.owner_id == user_id) {
                owner.value = true;
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

    } catch (error) {
        errorMessage.value = "Erreur de chargement des utilisateurs du serveur";
    }
};

const fetchChannelsInGuild = async () => {
    try {
        const response = await fetch(`http://localhost:8080/channels/guilds/${guildId.value}`, {
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

const deleteChannel = async (channelId) => {
    try {
        const response = await fetch(`http://localhost:8080/channels/${channelId}/guilds/${guildId.value}`, {
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

    if (!channelId) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/messages/channel/${channelId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            return;
        }

        messages.value = await response.json();
        actualChannel.value = channelId;

        // va chercher les messages du channel a interval régulier
        stopAutoRefresh(); // arrête l'ancien pooling
        startAutoRefresh();

    } catch (error) {
        errorMessage.value = "Erreur lors du chargement du channel : " + error;
    }
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
        alert("L'utilisateur a bien été banni du serveur");
        await fetchUsersInGuild();

    } catch (error) {
        errorMessage.value = "Erreur du bannissement de l'utilisateur : " + error;
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
        errorMessage.value = "Erreur de l'expulsion de l'utilisateur : " + error;
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

        router.push("/serverList");
    } catch (error) {
        errorMessage.value = "Erreur de la suppression du serveur : " + error;
        router.push("/serverList");
    }
};

const sendMessage = async () => {
    // A FIX CECI ACCEPTE MEME LES MSG VIDE VISIBLEMENT
    if (newMessage === "") {
        errorMessage.value = "Le message ne peut pas être vide";
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/messages/channel/${actualChannel.value}/send`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ 
                content: newMessage.value 
            }),
        });

        if (!response.ok) {
            errorMessage.value = "Erreur interne";
            return;
        }

        newMessage.value = ""; // remise a 0 du msg pour eviter de devoir supprimer son ancien msg a chaque fois lol
        errorMessage.value = null; // reset de l'erreur

    } catch (error) {
        errorMessage.value = "Erreur lors de l'envoi du message : " + error;
    }
}

// Charge les msg toutes les 3 secs
const startAutoRefresh = () => {
    refreshInterval = setInterval(() => {
        getChannelMessages(actualChannel.value);
    }, 3000);
};

// arrête le pooling
// A ne surtout pas ENLEVER, sans ça la requete va se dédoubler
const stopAutoRefresh = () => {
    clearInterval(refreshInterval);
    refreshInterval = null;
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
    if (user_id === userId) return;

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
    if (action === 'delete') {
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

// arrete le pooling quand on change de page
onUnmounted(() => {
    stopAutoRefresh();
});

</script>

<template>
    <div class="pr-64 pl-64">
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
            <!-- Si pas de channel choisi ou 0 message dans les channels, l'admin est 'invité' à ajouter des utilisateurs -->
            <div v-if="messages.length === 0 && owner" class="flex justify-center items-center">
                <div class="min-h-screen flex flex-col items-center justify-center bg-gray-900 text-white space-y-4">
                    <h1 class="text-4xl font-bold text-center">Il n'y a aucun message ici ! Changez de channel ou invitez des gens sur le serveur</h1>
                    <input v-model="username" @input="searchUsers" type="text" placeholder="Ajoutez quelqu'un"
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

            <!-- Affichage des messages -->
            <!-- A FIX L'AFFICHAGE DES MSG EST UN PEU RANDOM jusqu'a ce qu'on relance le serv  -->
            <div class="mt-6 w-full bg-gray-800 p-4 rounded-lg">
                <h2 class="text-xl font-bold mb-4">Messages</h2>
                <ul>
                    <li v-for="message in messages" :key="message.id" class="mb-2">
                        <div class="flex items-start space-x-4"
                            :class="{ 'justify-end': message.sender_id === user_id }">
                            <div class="text-sm font-bold text-purple-400">{{ message.username }}</div>
                            <div class="text-sm text-blue-300">{{ message.content }}</div>
                            <!-- A FIX Faut mettre un format de date + stylé -->
                            <div class="text-xs text-gray-300 ml-auto">{{ message.sent_at }}</div>
                        </div>
                    </li>
                </ul>
            </div>

            <!-- Liste des channels -->
            <div
                class="fixed left-0 top-2 bottom-0 w-64 bg-gray-800 p-4 border-r-4 border-gray-700 overflow-y-auto mt-16">
                <h2 class="text-xl font-bold mb-4">Channels</h2>
                <ul>
                    <li v-for="channel in channels_in_guild" :key="channel.channelId"
                        class="flex items-center justify-between px-3 py-2 rounded-lg hover:bg-gray-700"
                        @click="getChannelMessages(channel.channelId)"
                        @contextmenu.prevent="displayMenuChannel($event, channel.channelId)">
                        {{ channel.name }}
                    </li>
                </ul>
                <button v-if="owner" @click="createChannelRedirect()"
                    class="ml-auto px-4 py-1 bg-purple-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
                    Nouveau channel
                </button>

                <!-- affichage du menu clic droit pour les channels -->
                <MenuView v-if="showMenuChannel && owner" :actions="contextMenuActionsChannel"
                    @action-clicked="handleMenuActionsChannel" :x="menuX" :y="menuY" />
            </div>

            <!-- textbox pour ecire des messages -->
            <div class="fixed bottom-0 left-64 right-64 bg-gray-800 p-4 border-t-4 border-gray-700">
                <div class="flex items-center space-x-4">
                    <textarea v-model="newMessage" placeholder="Écrire un message"
                        class="flex-1 p-2 rounded-lg bg-gray-900 text-white border border-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                    <!-- A FIX le rendre disponible QUE SI CHANNEL NON NULL + MESSAGE NON NULL -->
                    <button @click="sendMessage"
                        class="px-4 py-2 bg-blue-500 hover:bg-blue-600 text-white font-semibold rounded-lg">
                        Envoyer
                    </button>
                </div>
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
                        <MenuView v-if="showMenuUser && owner" :actions="contextMenuActionsUser"
                            @action-clicked="handleMenuActionsUser" :x="menuX" :y="menuY" />
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
