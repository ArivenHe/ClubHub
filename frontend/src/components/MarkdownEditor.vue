<template>
  <div class="md-editor">
    <div class="md-toolbar">
      <button type="button" class="md-tool-btn" title="一级标题" @mousedown.prevent @click="insertHeading(1)">H1</button>
      <button type="button" class="md-tool-btn" title="二级标题" @mousedown.prevent @click="insertHeading(2)">H2</button>
      <button type="button" class="md-tool-btn" title="加粗" @mousedown.prevent @click="wrapSelection('**', '**', '加粗文本')">B</button>
      <button type="button" class="md-tool-btn" title="斜体" @mousedown.prevent @click="wrapSelection('*', '*', '斜体文本')">I</button>
      <button type="button" class="md-tool-btn" title="引用" @mousedown.prevent @click="prefixSelection('> ', '引用内容')">"</button>
      <button type="button" class="md-tool-btn" title="无序列表" @mousedown.prevent @click="prefixSelection('- ', '列表项')">UL</button>
      <button type="button" class="md-tool-btn" title="有序列表" @mousedown.prevent @click="prefixSelection('1. ', '列表项')">OL</button>
      <button type="button" class="md-tool-btn" title="行内代码" @mousedown.prevent @click="wrapSelection('`', '`', 'code')">{ }</button>
      <button type="button" class="md-tool-btn" title="代码块" @mousedown.prevent @click="insertCodeBlock">```</button>
      <button type="button" class="md-tool-btn" title="链接" @mousedown.prevent @click="insertLink">Link</button>
      <button type="button" class="md-tool-btn" title="分隔线" @mousedown.prevent @click="insertDivider">---</button>
    </div>

    <div class="md-body" :class="{ 'preview-hidden': !showPreview }">
      <textarea
        ref="textarea"
        class="md-input"
        :rows="rows"
        :maxlength="maxlength || null"
        :placeholder="placeholder"
        :value="modelValue"
        @input="onInput"
        @keydown.tab.prevent="insertSpaces"
      />
      <div v-if="showPreview" class="markdown-body md-preview" v-html="previewHtml"></div>
    </div>
    <div class="md-footer">
      <span class="muted">支持 Markdown 语法</span>
      <span class="muted">字数：{{ plainLength }}</span>
    </div>
  </div>
</template>

<script>
import { renderMarkdown } from '../utils/markdown'

export default {
  name: 'MarkdownEditor',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    },
    rows: {
      type: Number,
      default: 12
    },
    maxlength: {
      type: Number,
      default: 0
    },
    showPreview: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:modelValue'],
  computed: {
    previewHtml() {
      return renderMarkdown(this.modelValue || '')
    },
    plainLength() {
      return (this.modelValue || '').replace(/\s+/g, '').length
    }
  },
  methods: {
    onInput(event) {
      this.$emit('update:modelValue', event.target.value)
    },
    emitFromTextarea(textarea) {
      this.$emit('update:modelValue', textarea.value)
      textarea.focus()
    },
    withSelection(handler) {
      const textarea = this.$refs.textarea
      if (!textarea) {
        return
      }
      const start = textarea.selectionStart
      const end = textarea.selectionEnd
      const selected = textarea.value.slice(start, end)
      handler(textarea, start, end, selected)
    },
    wrapSelection(before, after, fallbackText) {
      this.withSelection((textarea, start, end, selected) => {
        const content = selected || fallbackText
        textarea.setRangeText(`${before}${content}${after}`, start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    },
    prefixSelection(prefix, fallbackText) {
      this.withSelection((textarea, start, end, selected) => {
        const content = selected || fallbackText
        const replaced = String(content)
          .split('\n')
          .map((line) => `${prefix}${line}`)
          .join('\n')
        textarea.setRangeText(replaced, start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    },
    insertHeading(level) {
      const prefix = `${'#'.repeat(level)} `
      this.prefixSelection(prefix, '标题')
    },
    insertCodeBlock() {
      this.withSelection((textarea, start, end, selected) => {
        const content = selected || 'code'
        textarea.setRangeText(`\n\`\`\`\n${content}\n\`\`\`\n`, start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    },
    insertLink() {
      this.withSelection((textarea, start, end, selected) => {
        const content = selected || '链接文本'
        textarea.setRangeText(`[${content}](https://example.com)`, start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    },
    insertDivider() {
      this.withSelection((textarea, start, end) => {
        textarea.setRangeText('\n---\n', start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    },
    insertSpaces() {
      this.withSelection((textarea, start, end) => {
        textarea.setRangeText('  ', start, end, 'end')
        this.emitFromTextarea(textarea)
      })
    }
  }
}
</script>

<style scoped>
.md-editor {
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface);
  overflow: hidden;
}

.md-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px;
  border-bottom: 1px solid var(--line);
  background: var(--surface-soft);
}

.md-tool-btn {
  border: 1px solid var(--line-strong);
  background: var(--surface);
  color: var(--text-main);
  border-radius: 8px;
  padding: 5px 8px;
  min-width: 36px;
  font-size: 12px;
  font-weight: 700;
  box-shadow: none;
  filter: none;
  transform: none;
}

.md-tool-btn:hover {
  border-color: var(--primary);
  background: var(--primary-soft);
  filter: none;
  transform: none;
}

.md-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  min-height: 280px;
}

.md-body.preview-hidden {
  grid-template-columns: 1fr;
}

.md-input {
  min-height: 280px;
  border: none;
  border-right: 1px solid var(--line);
  border-radius: 0;
  resize: vertical;
  background: var(--input-bg);
}

.md-body.preview-hidden .md-input {
  border-right: none;
}

.md-input:focus {
  box-shadow: none;
}

.md-preview {
  margin: 0;
  border: none;
  border-radius: 0;
  min-height: 280px;
  max-height: 520px;
  overflow: auto;
  background: var(--markdown-bg);
}

.md-footer {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 8px 10px;
  border-top: 1px solid var(--line);
  background: var(--surface-soft);
}

@media (max-width: 980px) {
  .md-body {
    grid-template-columns: 1fr;
  }

  .md-input {
    border-right: none;
    border-bottom: 1px solid var(--line);
  }
}
</style>
