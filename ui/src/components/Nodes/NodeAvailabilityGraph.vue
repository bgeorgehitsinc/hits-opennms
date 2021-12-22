<template>
  <div class="card">
    <div class="title headline3">Availability (last 24 hours)</div>

    <div class="flex-container availability-header headline4">
      <div>Availability</div>
      <div class="timeline" ref="timeline">{{ availability.availability }}%</div>
    </div>

    <template v-for="ipinterface of availability.ipinterfaces">
      <div v-if="ipinterface.services.length">
        <hr class="divider" />
        <div class="flex-container">
          <div class="service subtitle2">{{ ipinterface.address }}</div>
          <div class>
            <img
              :src="`${baseUrl}/opennms/rest/timeline/header/${startTime}/${endTime}/${width}`"
              :data-imgsrc="`/opennms/rest/timeline/header/${startTime}/${endTime}/`"
            />
          </div>
        </div>
      </div>

      <template v-for="service of ipinterface.services">
        <div class="flex-container">
          <div class="service subtitle2">{{ service.name }}</div>
          <div>
            <img
              :src="`${baseUrl}/opennms/rest/timeline/image/${nodeId}/${ipinterface.address}/${service.name}/${startTime}/${endTime}/${width}`"
            />
          </div>
          <div class="percentage subtitle2">{{ service.availability }}%</div>
        </div>
      </template>
    </template>
  </div>
</template>
  
<script setup lang="ts">
import { onMounted, ref, onUnmounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import { debounce } from 'lodash'

const baseUrl = ref(import.meta.env.VITE_BASE_URL || '')
const store = useStore()
const route = useRoute()
const nodeId = ref(route.params.id as string)
const now = dayjs()
const startTime = ref(now.subtract(1, 'day').unix())
const endTime = ref(now.unix())
const width = ref(200)
const timeline = ref<any>(null)
const recalculateWidth = () => {
  width.value = timeline.value.clientWidth - 60
}

onMounted(async () => {
  store.dispatch('nodesModule/getNodeAvailabilityPercentage', nodeId.value)
  recalculateWidth()
  window.addEventListener("resize", debounce(recalculateWidth, 100))
})

const availability = computed(() => store.state.nodesModule.availability)

onUnmounted(() => window.removeEventListener("resize", recalculateWidth))
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
  .title {
    padding: 5px 10px 0px 10px;
  }
}
.service {
  min-width: 103px;
  margin-left: 8px;
}
.timeline {
  flex-grow: 1;
  text-align: end;
}
.availability-header {
  padding: 0px 0px 0px 10px;
}
.percentage {
  margin-left: 3px;
}
.divider {
  width: 98%;
}
</style>