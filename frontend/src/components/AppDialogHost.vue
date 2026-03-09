<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="dialogState.visible" class="dialog-overlay" @click.self="onOverlayClick">
        <section class="app-dialog" role="dialog" aria-modal="true" aria-labelledby="app-dialog-title">
          <h3 id="app-dialog-title">{{ dialogState.title }}</h3>
          <p v-if="dialogState.message" class="app-dialog-message">{{ dialogState.message }}</p>
          <div v-if="dialogState.type === 'prompt'">
            <input
              ref="promptInput"
              v-model="dialogState.value"
              class="app-dialog-input"
              :type="dialogState.inputType"
              :placeholder="dialogState.placeholder"
              @keydown.enter.prevent="confirmDialog"
            />
            <p v-if="dialogState.errorMessage" class="app-dialog-error">{{ dialogState.errorMessage }}</p>
          </div>
          <div class="app-dialog-actions">
            <button
              v-if="dialogState.type !== 'alert'"
              type="button"
              class="small-btn secondary-btn-inline"
              @click="cancelDialog"
            >
              {{ dialogState.cancelText }}
            </button>
            <button type="button" @click="confirmDialog">{{ dialogState.confirmText }}</button>
          </div>
        </section>
      </div>
    </transition>
  </teleport>
</template>

<script>
import { nextTick } from 'vue'
import {
  closeDialogCancel,
  closeDialogConfirm,
  dialogState
} from '../utils/dialog'

export default {
  name: 'AppDialogHost',
  data() {
    return {
      dialogState
    }
  },
  watch: {
    'dialogState.visible': {
      async handler(visible) {
        if (!visible || this.dialogState.type !== 'prompt') {
          return
        }
        await nextTick()
        const input = this.$refs.promptInput
        if (input && typeof input.focus === 'function') {
          input.focus()
        }
        if (input && typeof input.select === 'function') {
          input.select()
        }
      }
    }
  },
  methods: {
    confirmDialog() {
      closeDialogConfirm()
    },
    cancelDialog() {
      closeDialogCancel()
    },
    onOverlayClick() {
      if (this.dialogState.type !== 'alert') {
        closeDialogCancel()
      }
    }
  }
}
</script>
