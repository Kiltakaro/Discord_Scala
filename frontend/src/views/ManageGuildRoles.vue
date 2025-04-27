<template>
    <div class="p-6 max-w-3xl mx-auto bg-gray-800 text-white rounded-xl shadow-md">
        <h2 class="text-2xl font-bold mb-6 text-center">Manage Server Roles</h2>

        <div class="flex justify-end mb-4">
            <button @click="showAddRoleForm = true"
                class="px-4 py-2 bg-green-600 hover:bg-green-700 rounded-lg font-semibold">
                Ajouter un rôle
            </button>
        </div>

        <div v-if="loading" class="text-center text-gray-400">Loading roles...</div>

        <div v-else-if="error" class="text-center text-red-400">{{ error }}</div>

        <div v-else>
            <div v-if="roles.length > 0">
                <ul class="space-y-2">
                    <li v-for="role in roles" :key="role.role_id"
                        class="bg-gray-700 p-4 rounded-lg flex justify-between items-center">
                        <div>
                            <p class="font-semibold">{{ role[1] }}</p>
                            <p class="text-sm text-gray-400">Priority: {{ role[2] }}</p>
                        </div>
                        <div class="flex space-x-2">
                            <button @click="startEditing(role)"
                                class="px-3 py-1 bg-yellow-500 hover:bg-yellow-600 rounded-lg text-black font-semibold">
                                Edit
                            </button>
                            <button @click="deleteRole(role[0])"
                                class="px-3 py-1 bg-red-600 hover:bg-red-700 rounded-lg font-semibold">
                                Delete
                            </button>
                        </div>
                    </li>
                </ul>
            </div>
            <div v-else class="text-center text-gray-400">
                No roles found.
            </div>
        </div>

        <!-- Add Role Form -->
        <div v-if="showAddRoleForm" class="mt-8">
            <h3 class="text-xl font-bold mb-4 text-center">Add New Role</h3>
            <div class="space-y-4">
                <input v-model="newRoleName" type="text" placeholder="Role name"
                    class="w-full p-2 rounded-lg bg-gray-700 text-white" />
                <input v-model.number="newRolePriority" type="number" placeholder="Priority"
                    class="w-full p-2 rounded-lg bg-gray-700 text-white" />
                <div class="flex justify-center">
                    <button @click="addRole" class="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg font-semibold">
                        Ajouter
                    </button>
                </div>
            </div>
        </div>

        <!-- Edit Role Form -->
        <div v-if="editingRole" class="mt-8">
            <h3 class="text-xl font-bold mb-4 text-center">Edit Role</h3>
            <div class="space-y-4">
                <input v-model="editRoleName" type="text" class="w-full p-2 rounded-lg bg-gray-700 text-white" />
                <input v-model.number="editRolePriority" type="number"
                    class="w-full p-2 rounded-lg bg-gray-700 text-white" />
                <div class="flex justify-center space-x-4">
                    <button @click="updateRole"
                        class="px-6 py-2 bg-yellow-500 hover:bg-yellow-600 rounded-lg text-black font-semibold">
                        Save
                    </button>
                    <button @click="cancelEditing"
                        class="px-6 py-2 bg-gray-500 hover:bg-gray-600 rounded-lg font-semibold">
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const guildId = route.params.id || route.params.guildId // depending how you pass it

const roles = ref([])
const loading = ref(true)
const error = ref('')
const showAddRoleForm = ref(false)
const newRoleName = ref('')
const newRolePriority = ref(1)

const editingRole = ref(null)
const editRoleName = ref('')
const editRolePriority = ref(1)

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
        const res = await fetchWithAuth(`/roles/${guildId}`)
        if (!res.ok) throw new Error('Failed to fetch roles')
        roles.value = await res.json()
    } catch (err) {
        console.error(err)
        error.value = err.message || 'An error occurred'
    } finally {
        loading.value = false
    }
}

const addRole = async () => {
    if (!newRoleName.value || newRolePriority.value === null) return
    try {
        await fetchWithAuth('/roles/create', {
            method: 'POST',
            body: JSON.stringify({
                guildId,
                name: newRoleName.value,
                priority: newRolePriority.value
            })
        })
        newRoleName.value = ''
        newRolePriority.value = 1
        showAddRoleForm.value = false
        await fetchRoles()
    } catch (err) {
        console.error('Failed to add role:', err)
    }
}

const deleteRole = async (roleId) => {
    try {
        await fetchWithAuth(`/roles/${roleId}`, {
            method: 'DELETE'
        })
        await fetchRoles()
    } catch (err) {
        console.error('Failed to delete role:', err)
    }
}

const startEditing = (role) => {
    editingRole.value = role
    editRoleName.value = role.role_name
    editRolePriority.value = role.priority
}

const updateRole = async () => {
    if (!editingRole.value) return
    try {
        await fetchWithAuth('/roles', {
            method: 'PUT',
            body: JSON.stringify({
                roleId: editingRole.value[0],
                guildId: guildId,
                name: editRoleName.value,
                priority: editRolePriority.value
            })
        })
        editingRole.value = null
        await fetchRoles()
    } catch (err) {
        console.error('Failed to update role:', err)
    }
}


const cancelEditing = () => {
    editingRole.value = null
}

onMounted(fetchRoles)
</script>