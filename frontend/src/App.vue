<template>
  <router-view />
  <app-dialog-host />
</template>

<script>
import AppDialogHost from './components/AppDialogHost.vue'

export default {
  name: 'AppRoot',
  components: {
    AppDialogHost
  }
}
</script>
