<template>
  <FeatherAutocomplete
    v-model="searchStr"
    type="multi"
    :results="results"
    label="Search"
    class="map-search"
    @search="resetLabelsAndSearch"
    :loading="loading"
    :hideLabel="true"
    text-prop="label"
    @update:modelValue="selectItem"
    :labels="labels"
  ></FeatherAutocomplete>
</template>
  
<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash'
import { useStore } from 'vuex'
import { FeatherAutocomplete } from '@featherds/autocomplete'

const emit = defineEmits(['fly-to-node', 'set-bounding-box'])

const store = useStore()
const searchStr = ref()
const loading = ref(false)
const defaultLabels = { noResults: 'Searching...' }
const labels = ref(defaultLabels)

const selectItem = (items: { label: string }[]) => {
  const nodeLabels = items.map((item) => item.label)
  store.dispatch('mapModule/setSearchedNodeLabels', nodeLabels)
  if (nodeLabels.length) {
    if (nodeLabels.length === 1) {
      // fly to last selected node
      emit('fly-to-node', nodeLabels[0])
    } else {
      // set bounding box for all searched nodes
      emit('set-bounding-box', nodeLabels)
    }
  }
}

const resetLabelsAndSearch = (value: string) => {
  labels.value = defaultLabels
  search(value)
}

const search = debounce(async (value: string) => {
  if (!value) return
  loading.value = true
  await store.dispatch('searchModule/search', value)
  labels.value = { noResults: 'No results found' }
  loading.value = false
}, 1000)

const results = computed(() => {
  if (store.state.searchModule.searchResults[0]) {
    return store.state.searchModule.searchResults[0].results
  }
  return []
})
</script>
  
<style lang="scss">
.map-search {
  z-index: 1000;
  width: 290px !important;
  .feather-input-border {
    background: var(--feather-surface);
  }
  &.feather-autocomplete-container {
    padding-top: 0px;
  }
  .feather-autocomplete-input {
    height: 25px !important;
  }
}
</style>
