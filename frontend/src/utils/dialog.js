import { reactive } from 'vue'

const DEFAULT_TITLE = {
  alert: '提示',
  confirm: '请确认',
  prompt: '请输入'
}

const queue = []
let currentTask = null

export const dialogState = reactive({
  visible: false,
  type: 'alert',
  title: '',
  message: '',
  placeholder: '',
  inputType: 'text',
  value: '',
  confirmText: '确定',
  cancelText: '取消',
  validator: null,
  errorMessage: ''
})

function applyConfig(config) {
  dialogState.type = config.type
  dialogState.title = config.title || DEFAULT_TITLE[config.type] || DEFAULT_TITLE.alert
  dialogState.message = config.message || ''
  dialogState.placeholder = config.placeholder || ''
  dialogState.inputType = config.inputType || 'text'
  dialogState.value = config.initialValue || ''
  dialogState.confirmText = config.confirmText || '确定'
  dialogState.cancelText = config.cancelText || '取消'
  dialogState.validator = config.validator || null
  dialogState.errorMessage = ''
  dialogState.visible = true
}

function resetState() {
  dialogState.visible = false
  dialogState.type = 'alert'
  dialogState.title = ''
  dialogState.message = ''
  dialogState.placeholder = ''
  dialogState.inputType = 'text'
  dialogState.value = ''
  dialogState.confirmText = '确定'
  dialogState.cancelText = '取消'
  dialogState.validator = null
  dialogState.errorMessage = ''
}

function openNextDialog() {
  if (currentTask || queue.length === 0) {
    return
  }
  currentTask = queue.shift()
  applyConfig(currentTask.config)
}

function enqueueDialog(config) {
  return new Promise((resolve) => {
    queue.push({ config, resolve })
    openNextDialog()
  })
}

function finishCurrent(result) {
  if (!currentTask) {
    return
  }
  const resolve = currentTask.resolve
  currentTask = null
  resetState()
  resolve(result)
  openNextDialog()
}

export function showAlert(options = {}) {
  return enqueueDialog({
    type: 'alert',
    ...options
  })
}

export function showConfirm(options = {}) {
  return enqueueDialog({
    type: 'confirm',
    ...options
  })
}

export function showPrompt(options = {}) {
  return enqueueDialog({
    type: 'prompt',
    ...options
  })
}

export function closeDialogCancel() {
  if (dialogState.type === 'prompt') {
    finishCurrent(null)
    return
  }
  finishCurrent(false)
}

export function closeDialogConfirm() {
  if (dialogState.type === 'prompt') {
    const validator = dialogState.validator
    const text = String(dialogState.value || '')
    const validationMessage = typeof validator === 'function' ? validator(text) : ''
    if (validationMessage) {
      dialogState.errorMessage = validationMessage
      return
    }
    finishCurrent(text)
    return
  }
  finishCurrent(true)
}
