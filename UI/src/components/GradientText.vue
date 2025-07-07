<template>
    <div :class="['animated-gradient-text', customClass]">
        <div v-if="showBorder" class="gradient-overlay" :style="gradientStyle"></div>
        <div class="text-content" :style="gradientStyle">
            <slot></slot>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import './GradientText.css';

const props = defineProps({
    customClass: {
        type: String,
        default: ''
    },
    colors: {
        type: Array,
        default: () => ["#40ffaa", "#4079ff", "#40ffaa", "#4079ff", "#40ffaa"]
    },
    animationSpeed: {
        type: Number,
        default: 16
    },
    showBorder: {
        type: Boolean,
        default: false
    }
});

const gradientStyle = computed(() => ({
    backgroundImage: `linear-gradient(to right, ${props.colors.join(", ")})`,
    animationDuration: `${props.animationSpeed}s`,
}));
</script>