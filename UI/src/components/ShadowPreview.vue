<template>
    <div ref="host" class="shadow-host"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
    content: {
        type: String,
        default: ''
    }
});

const host = ref(null);
let shadowRoot = null;

const updateContent = () => {
    if (!shadowRoot) return;

    if (props.content) {
        // Reset styles to prevent layout issues within the preview
        // These match the previous CSS safeguards but applied inside Shadow DOM
        const resetStyles = `
      <style>
        :host {
          display: block;
          overflow: hidden; /* Ensure no scrollbars on the host itself if content overflows */
        }
        /* Basic reset for common elements */
        * {
            box-sizing: border-box;
            /* Replicate original safe-guards */
            max-width: 100%;
            position: relative;
        }
        
        /* 
           The original code had aggressive !important rules. 
           We keep them but scoped to Shadow DOM so they don't leak.
        */
        body, html {
            width: 100% !important;
            height: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
            overflow: visible !important;
            /* Reset max-width/position for body/html as they are containers */
            max-width: none;
            position: static;
        }
        
        /* Ensure images and other media don't overflow */
        img, video, iframe {
            max-width: 100%;
        }
      </style>
    `;
        shadowRoot.innerHTML = resetStyles + props.content;
    } else {
        shadowRoot.innerHTML = '';
    }
};

onMounted(() => {
    if (host.value) {
        shadowRoot = host.value.attachShadow({ mode: 'open' });
        updateContent();
    }
});

watch(() => props.content, () => {
    updateContent();
});
</script>

<style scoped>
.shadow-host {
    display: block;
    /* Remove width/height 100% to allow parent styles to control dimensions */
}
</style>
