<script setup>
import { defineProps, defineEmits } from 'vue'

const { actions, x, y } = defineProps(['actions', 'x', 'y']);
const emit = defineEmits(['action-clicked']);

const emitAction = (action) => {
    emit('action-clicked', action);
};

</script>

<template>
    <div
        class="fixed bg-gray-500 rounded-md border-0.125rem border-gray-60"
        :style="{ top: y + 'px', left: x + 'px' }"
    >
        <div
            v-for="action in actions"
            :key="action.action"
            @click="emitAction(action.action)"
            class="p-2.5 hover:bg-gray-400 rounded-md cursor-pointer"
        >
            {{ action.label }}
        </div>
    </div>
</template>
