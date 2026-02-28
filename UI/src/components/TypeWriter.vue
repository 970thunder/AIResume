<template>
    <span class="typewriter-wrapper">
        <span class="typewriter-text">{{ displayText }}</span>
        <span class="cursor" v-if="showCursor">|</span>
    </span>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
    text: {
        type: String,
        required: true
    },
    speed: {
        type: Number,
        default: 100
    },
    delay: {
        type: Number,
        default: 0
    },
    showCursor: {
        type: Boolean,
        default: true
    },
    loop: {
        type: Boolean,
        default: false
    },
    loopDelay: {
        type: Number,
        default: 2000
    }
});

const displayText = ref('');
const typeIndex = ref(0);
let timeoutId = null;

const typeWriter = () => {
    if (typeIndex.value < props.text.length) {
        displayText.value = props.text.slice(0, typeIndex.value + 1);
        typeIndex.value++;
        timeoutId = setTimeout(typeWriter, props.speed);
    } else if (props.loop) {
        timeoutId = setTimeout(() => {
            typeIndex.value = 0;
            displayText.value = '';
            typeWriter();
        }, props.loopDelay);
    }
};

const startTyping = () => {
    timeoutId = setTimeout(typeWriter, props.delay);
};

onMounted(() => {
    startTyping();
});

watch(() => props.text, () => {
    if (timeoutId) clearTimeout(timeoutId);
    typeIndex.value = 0;
    displayText.value = '';
    startTyping();
});
</script>

<style scoped>
.typewriter-wrapper {
    display: inline;
}

.typewriter-text {
    background: linear-gradient(135deg, #38bdf8 0%, #a5f3fc 50%, #818cf8 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-size: 200% 200%;
    animation: gradientFlow 3s ease infinite;
}

.cursor {
    display: inline-block;
    color: #38bdf8;
    animation: blink 1s step-end infinite;
    margin-left: 2px;
    font-weight: 300;
}

@keyframes blink {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

@keyframes gradientFlow {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}
</style>
