<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
   <div class="scv-container">
    <div class="list"><SCVListVue /></div>
    <div class="form"><SCVFormVue /></div>
  </div>
</template>

<script setup lang="ts">
import SCVListVue from '@/components/SCV/SCVList.vue'
import SCVFormVue from '@/components/SCV/SCVForm.vue'
import { useStore } from 'vuex'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { BreadCrumb } from '@/types'

const store = useStore()

const homeUrl = computed<string>(() => store.state.menuModule.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Secure Credentials Vault', to: '#', position: 'last' }
  ]
})

onMounted(() => store.dispatch('scvModule/getAliases'))
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";

.scv-container {
  padding: 2px;
  margin-left: 2px;
  display: flex;
  flex-grow: 1;
  gap: 2px;

  .list {
    min-width: 200px;
    max-width: 350px;
  }
  .form {
    width: 600px;
  }
}
</style>
