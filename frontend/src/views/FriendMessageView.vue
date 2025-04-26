<script setup>
// tuto context menu (goatesque): https://medium.com/@sj.anyway/custom-right-click-context-menu-in-vue3-b323a3913684
import { ref, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const router = useRouter();

const errorMessage = ref(null);
const token = localStorage.getItem("token");
const user_id = localStorage.getItem("user_id");
const messages = ref([]); // c'est les msg du channel
const newMessage = ref(""); // c'est le msg que le user écrit
const actualChannel = ref(null); // channel actuel
const route = useRoute();
const friendId = ref(route.params.id);

let refreshInterval = ref(null); // Pour le chargement des messages
const friendshipId = ref(null);
const channelCreated = ref(false); // savoir si le channel a été créé (cas particulier)

console.log(token); // Pour les tests postman
if (!token) {
    router.push("/login");
}

if (user_id == friendId) {
    errorMessage.value = "Vous ne pouvez pas vous envoyer de message à vous même";
    router.push("/friends");
}

const getFriendshipIdFromUserIds = async () => {
    try {
        const response = await fetch(`http://localhost:8080/friends/${friendId.value}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!response.ok) {
            errorMessage.value = `Erreur lors de la récupération de l'ID de l'amitié` ;
            return;
        }

        const data = await response.json();
        friendshipId.value = data;

    } catch (error) {
        errorMessage.value = "Erreur lors de la récupération de l'ID de l'amitié : " + error;
    }
}

// const createChannelIfNotExists = async () => {
//     try {
//         // verification si le channel existe 
//         const response = await fetch(`http://localhost:8080/channels/friends/${friendshipId.value}`, {
//             method: "GET",
//             headers: {
//                 "Content-Type": "application/json",
//                 "Authorization": `Bearer ${token}`,
//             },
//         });

//         if (response.status === 404) {
//             try {
//                 const responseCreateChannel = await fetch(`http://localhost:8080/channels/friends/${friendshipId.value}`, {
//                     method: "POST",
//                     headers: {
//                         "Content-Type": "application/json",
//                         "Authorization": `Bearer ${token}`,
//                     },
//                 });
//                 if (!responseCreateChannel.ok) {
//                     return;
//                 }

//             } catch (error) {
//                 errorMessage.value = "Erreur lors de la creation du DM channel : " + error;
//             }
//         }
//         if (!response.ok) {
//             return;
//         }

//         // inutile de recup ici
//         actualChannel.value = data.channel_id;

//     } catch (error) {
//         errorMessage.value = "Erreur lors de la recherche du channel : " + error;
//     }
// }



const createChannel = async () => {

    try {
        const responseCreateChannel = await fetch(`http://localhost:8080/channels/friends/${friendshipId.value}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });
        if (!responseCreateChannel.ok) {
            return;
        }

        createChanneld.value = true;

    } catch (error) {
        errorMessage.value = "Erreur lors de la creation du DM channel : " + error;
    }
}



const getChannelFromFriendship = async () => {

    try {
        // verification si le channel existe 
        const response = await fetch(`http://localhost:8080/channels/friends/${friendshipId.value}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        // si y'a pas de channel dm faut en créer (404) = jvous apprends rien j'espere
        if (response.status === 404) {
            await createChannel();
            return;
        }

        const data = await response.json();
        actualChannel.value = data.channelId;

    } catch (error) {
        errorMessage.value = "Erreur lors de la recherche du channel : " + error;
    }
}


const getChannelMessages = async () => {

    if (!friendshipId) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/messages/channel/${actualChannel.value}`, {
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

        // va chercher les messages du channel a interval régulier
        stopAutoRefresh(); // arrête l'ancien pooling (normalement y'en a pas mais bon)
        startAutoRefresh();

    } catch (error) {
        errorMessage.value = "Erreur lors du chargement du channel : " + error;
    }
}

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

// c'est un peu le bordel mais c'est logique
onMounted(async() => {
    await getFriendshipIdFromUserIds(); // on recup l'id de la friendship
    if (friendshipId.value) {
        await getChannelFromFriendship(); // grace a friendship_id on trouve le channel
        if (channelCreated.value) { // si aucun channel n'a été trouvé, alors ça crée un channel avec ce friendship_id
            await getChannelFromFriendship(); // on recherche le channel a partir de friendship_id a nouveau psk un channel existe forcément
        }
        if (actualChannel.value) { // mtn on peu fetch les msgs
             await getChannelMessages();
        }
    }
});

// arrete le pooling quand on change de page
onUnmounted(() => {
    stopAutoRefresh();
});

</script>

<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <p v-if="errorMessage" class="text-red-500">{{ errorMessage }}</p>


        <!-- Affichage des messages -->
        <!-- A FIX L'AFFICHAGE DES MSG EST UN PEU RANDOM jusqu'a ce qu'on relance le serv  -->
        <div class="mt-6 w-full bg-gray-800 p-4 rounded-lg">
            <h2 class="text-xl font-bold mb-4">Messages</h2>
            <ul>
                <li v-for="message in messages" :key="message.id" class="mb-2">
                    <div class="flex items-start space-x-4" :class="{ 'justify-end': message.sender_id === user_id }">
                        <div class="text-sm font-bold text-purple-400">{{ message.username }}</div>
                        <div class="text-sm text-blue-300">{{ message.content }}</div>
                        <!-- A FIX Faut mettre un format de date + stylé -->
                        <div class="text-xs text-gray-300 ml-auto">{{ message.sent_at }}</div>
                    </div>
                </li>
            </ul>
        </div>

        <!-- textbox pour ecire des messages -->
        <div class="fixed bottom-0 left-0 right-0 bg-gray-800 p-4 border-t-4 border-gray-700">
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

        <!-- On pourrait rajouter un truc pour afficher l'ami sur le coté droit-->

    </div>


</template>
