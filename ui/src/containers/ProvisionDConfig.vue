<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="wrapper feather-container center">
        <Snackbar />
        <ConfigurationHeader
          title="Configuration"
          headline="External Requisitions and Thread Pools"
        />
        <ConfigurationTableWrapper />
        <div class="spacer"></div>
        <ThreadPools />
      </div>
    </div>
  </div>
</template>

<script
  setup
  lang="ts"
>
import Snackbar from '@/components/Common/Snackbar.vue'
import ConfigurationHeader from '@/components/Configuration/ConfigurationHeader.vue'
import ConfigurationTableWrapper from '@/components/Configuration/ConfigurationTableWrapper.vue'
import ThreadPools from '@/components/Configuration/ConfigurationThreadPools.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { populateProvisionD } from '@/services/configurationService'
import { useStore } from 'vuex'
import { BreadCrumb } from '@/types'

const store = useStore()

const homeUrl = computed<string>(() => store.state.menuModule.mainMenu?.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'External Requisitions and Thread Pools', to: '#', position: 'last' }
  ]
})

populateProvisionD(store)
</script>
<style
  lang="scss"
  scoped
>
@import '@featherds/styles/mixins/typography';
@import '@featherds/styles/mixins/elevation';

.wrapper {
  margin-top: 20px;
  margin-left: 20px;
}
</style>

