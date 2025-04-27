<template>
    <div class="min-h-screen flex flex-col items-center bg-gray-900 text-white p-6">
        <h2 class="text-2xl font-bold mb-6 text-center">Gerer les roles de l'utilisateur {{ username || 'Chargement...'
            }}</h2>

        <div v-if="loading" class="text-center text-gray-400">Chargement des roles...</div>

        <div v-else-if="error" class="text-center text-red-400">{{ error }}</div>

        <div v-else>
            <!-- Roles du user -->
            <div class="mb-8">
                <h3 class="text-xl font-semibold mb-4">Roles de l'utilisateur :</h3>
                <ul class="space-y-2">
                    <li v-for="role in assignedRoles" :key="role[0]"
                        class="flex justify-between items-center bg-gray-700 p-2 rounded-lg">
                        <span>{{ role[1] }} (priority: {{ role[2] }})</span>
                        <button @click="removeRole(role[0])"
                            class="px-3 py-1 bg-red-600 hover:bg-red-700 rounded-lg font-semibold">
                            Enlever
                        </button>
                    </li>
                </ul>
                <div v-if="assignedRoles.length === 0" class="text-center text-gray-400 mt-4">Aucun role donné
                </div>
            </div>

            <!-- Autres assignables -->
            <div>
                <h3 class="text-xl font-semibold mb-4">Roles à donner à l'utilisateur</h3>
                <ul class="space-y-2">
                    <li v-for="role in availableRoles" :key="role[0]"
                        class="flex justify-between items-center bg-gray-700 p-2 rounded-lg">
                        <span>{{ role[1] }} (priority: {{ role[2] }})</span>
                        <button @click="addRole(role[0])"
                            class="px-3 py-1 bg-green-600 hover:bg-green-700 rounded-lg font-semibold">
                            Ajouter
                        </button>
                    </li>
                </ul>
                <div v-if="availableRoles.length === 0" class="text-center text-gray-400 mt-4">Aucun role à donner à
                    l'utilisateur
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const guildId = route.params.guildId
const userId = route.params.userId

const allRoles = ref([])
const assignedRoles = ref([])
const availableRoles = ref([])
const username = ref('')
const loading = ref(true)
const error = ref('')

// Helper pour encapsuler la vérif d'authentification
const fetchWithAuth = async (url, options = {}) => {
    const token = localStorage.getItem('token')
    return fetch(`http://localhost:8080${url}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            ...(options.headers || {})
        }
    })
}

const fetchRoles = async () => {
    loading.value = true
    error.value = ''
    try {
        const resUser = await fetchWithAuth(`/users/${userId}`)
        if (!resUser.ok) throw new Error('Failed to fetch user info')
        const userData = await resUser.json()
        username.value = userData.username

        const resAssigned = await fetchWithAuth(`/roles/assigned/${userId}/${guildId}`)
        if (!resAssigned.ok) throw new Error('Failed to fetch assigned roles')
        assignedRoles.value = await resAssigned.json()

        const resAll = await fetchWithAuth(`/roles/${guildId}`)
        if (!resAll.ok) throw new Error('Failed to fetch all roles')
        allRoles.value = await resAll.json()

        // Filtrer les rôles non assignés
        availableRoles.value = allRoles.value.filter(role =>
            !assignedRoles.value.some(ar => ar[0] === role[0])
        )

    } catch (err) {
        console.error(err)
        error.value = err.message || 'An unexpected error occurred'
    } finally {
        loading.value = false
    }
}

const removeRole = async (roleId) => {
    try {
        await fetchWithAuth('/roles/unassign', {
            method: 'DELETE',
            body: JSON.stringify({ userId, guildId, roleId })
        })
        await fetchRoles()
    } catch (err) {
        console.error('Failed to remove role:', err)
    }
}

const addRole = async (roleId) => {
    try {
        await fetchWithAuth('/roles/assign', {
            method: 'POST',
            body: JSON.stringify({ userId, guildId, roleId })
        })
        await fetchRoles()
    } catch (err) {
        console.error('Failed to add role:', err)
    }
}

onMounted(fetchRoles)
</script>