<script setup>
import { ref, onMounted } from 'vue'
import { API_URLS } from '@/config/api.js'

const sitePv = ref('--')
const siteUv = ref('--')

onMounted(async () => {
    try {
        // 1. Record the current visit. We don't need to wait for this to finish.
        fetch(API_URLS.visits.record, { method: 'POST' })

        // 2. Fetch the latest statistics.
        const response = await fetch(API_URLS.visits.stats)
        if (!response.ok) {
            throw new Error('Failed to fetch stats')
        }
        const stats = await response.json()

        // 3. Update the display values.
        sitePv.value = stats.sitePv
        siteUv.value = stats.siteUv
    } catch (error) {
        console.error('Error fetching visitor stats:', error)
        // Keep the default '--' values on error.
    }
})
</script>

<template>
    <div class="panel">
        <div class="container">
            <section class="grid">
                <span class="text">
                    本站总访问量 <span class="font-bold">{{ sitePv }}</span> 次
                </span>
                <span class="text">
                    本站访客数 <span class="font-bold">{{ siteUv }}</span> 人次
                </span>
            </section>
        </div>
    </div>
</template>

<style scoped>
.panel {
    margin-top: 12px;
    margin-bottom: 8px;
}

.container {
    background-color: var(--vp-c-bg-soft);
    border-radius: 8px;
    width: 100%;
    min-height: 32px;
    max-width: 1152px;
    margin-left: auto;
    margin-right: auto;
}

.grid {
    font-weight: 500;
    padding-top: 12px;
    padding-bottom: 12px;
    padding-left: 12px;
    padding-right: 12px;
    justify-items: center;
    align-items: center;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    display: grid;
}

.text {
    font-size: .875rem;
    line-height: 1.25rem;
}
</style>