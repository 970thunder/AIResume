<template>
    <p ref="elRef" :class="customClass" :style="{ display: 'flex', flexWrap: 'wrap' }">
        <span v-for="(segment, index) in elements" :key="index" class="inline-block will-change" v-motion
            :initial="fromSnapshot" :animate="inView ? animateKeyframes : fromSnapshot"
            :transition="getTransition(index)" @motion-complete="() => onMotionComplete(index)">
            {{ segment === ' ' ? '\u00A0' : segment }}{{ animateBy === 'words' && index < elements.length - 1 ? '\u00A0'
                : '' }} </span>
    </p>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';

const buildKeyframes = (from, steps) => {
    const keys = new Set([
        ...Object.keys(from),
        ...steps.flatMap((s) => Object.keys(s)),
    ]);

    const keyframes = {};
    keys.forEach((k) => {
        keyframes[k] = [from[k], ...steps.map((s) => s[k])];
    });
    return keyframes;
};

const props = defineProps({
    text: { type: String, default: '' },
    delay: { type: Number, default: 200 },
    customClass: { type: String, default: '' },
    animateBy: { type: String, default: 'words' },
    direction: { type: String, default: 'top' },
    threshold: { type: Number, default: 0.1 },
    rootMargin: { type: String, default: '0px' },
    animationFrom: { type: Object, default: null },
    animationTo: { type: Object, default: null },
    easing: { type: Function, default: (t) => t },
    stepDuration: { type: Number, default: 0.35 },
});

const emit = defineEmits(['animationComplete']);

const elements = computed(() => (props.animateBy === 'words' ? props.text.split(' ') : props.text.split('')));
const inView = ref(false);
const elRef = ref(null);

onMounted(() => {
    if (!elRef.value) return;
    const observer = new IntersectionObserver(
        ([entry]) => {
            if (entry.isIntersecting) {
                inView.value = true;
                observer.unobserve(elRef.value);
            }
        },
        { threshold: props.threshold, rootMargin: props.rootMargin }
    );
    observer.observe(elRef.value);
});

const defaultFrom = computed(() =>
    props.direction === 'top'
        ? { filter: 'blur(10px)', opacity: 0, y: -50 }
        : { filter: 'blur(10px)', opacity: 0, y: 50 }
);

const defaultTo = computed(() => [
    {
        filter: 'blur(5px)',
        opacity: 0.5,
        y: props.direction === 'top' ? 5 : -5,
    },
    { filter: 'blur(0px)', opacity: 1, y: 0 },
]);

const fromSnapshot = computed(() => props.animationFrom ?? defaultFrom.value);
const toSnapshots = computed(() => props.animationTo ?? defaultTo.value);

const animateKeyframes = computed(() => buildKeyframes(fromSnapshot.value, toSnapshots.value));

const getTransition = (index) => {
    const stepCount = toSnapshots.value.length + 1;
    const totalDuration = props.stepDuration * (stepCount - 1);
    const times = Array.from({ length: stepCount }, (_, i) =>
        stepCount === 1 ? 0 : i / (stepCount - 1)
    );

    const spanTransition = {
        duration: totalDuration,
        times,
        delay: (index * props.delay) / 1000,
        ease: props.easing,
    };
    return spanTransition;
};

const onMotionComplete = (index) => {
    if (index === elements.value.length - 1) {
        emit('animationComplete');
    }
};
</script>

<style scoped>
.inline-block {
    display: inline-block;
}

.will-change {
    will-change: transform, filter, opacity;
}
</style>