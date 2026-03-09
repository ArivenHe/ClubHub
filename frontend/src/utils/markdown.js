function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function sanitizeUrl(url) {
  const value = (url || '').trim()
  if (!value) {
    return '#'
  }
  const safePattern = /^(https?:\/\/|mailto:|\/)/i
  return safePattern.test(value) ? value : '#'
}

function normalizeHeadingText(raw) {
  return String(raw || '')
    .replace(/!\[[^\]]*]\([^)]+\)/g, '')
    .replace(/\[([^\]]+)]\([^)]+\)/g, '$1')
    .replace(/[`*_~]/g, '')
    .trim()
}

function slugifyHeading(text) {
  const cleaned = String(text || '')
    .toLowerCase()
    .replace(/[^a-z0-9\u4e00-\u9fa5\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-')
  return cleaned || 'section'
}

function allocateHeadingId(rawText, counterMap) {
  const base = slugifyHeading(normalizeHeadingText(rawText))
  const currentCount = counterMap.get(base) || 0
  const nextCount = currentCount + 1
  counterMap.set(base, nextCount)
  return nextCount === 1 ? base : `${base}-${nextCount}`
}

function parseInline(value) {
  if (!value) {
    return ''
  }

  let text = escapeHtml(value)

  text = text.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, (_match, alt, url) => {
    return `<img src="${sanitizeUrl(url)}" alt="${escapeHtml(alt)}" />`
  })

  text = text.replace(/\[([^\]]+)\]\(([^)]+)\)/g, (_match, label, url) => {
    return `<a href="${sanitizeUrl(url)}" target="_blank" rel="noopener noreferrer">${label}</a>`
  })

  text = text.replace(/`([^`]+)`/g, '<code>$1</code>')
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  text = text.replace(/__([^_]+)__/g, '<strong>$1</strong>')
  text = text.replace(/\*([^*]+)\*/g, '<em>$1</em>')
  text = text.replace(/_([^_]+)_/g, '<em>$1</em>')
  text = text.replace(/~~([^~]+)~~/g, '<del>$1</del>')

  return text
}

export function renderMarkdown(markdown) {
  if (!markdown || !String(markdown).trim()) {
    return ''
  }

  const lines = String(markdown).replace(/\r\n?/g, '\n').split('\n')
  let html = ''
  let inCode = false
  let codeLines = []
  let inUl = false
  let inOl = false
  const headingIdCounter = new Map()

  const closeLists = () => {
    if (inUl) {
      html += '</ul>'
      inUl = false
    }
    if (inOl) {
      html += '</ol>'
      inOl = false
    }
  }

  for (const line of lines) {
    if (line.trim().startsWith('```')) {
      if (!inCode) {
        closeLists()
        inCode = true
        codeLines = []
      } else {
        html += `<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`
        inCode = false
        codeLines = []
      }
      continue
    }

    if (inCode) {
      codeLines.push(line)
      continue
    }

    if (!line.trim()) {
      closeLists()
      continue
    }

    const heading = line.match(/^(#{1,6})\s+(.+)$/)
    if (heading) {
      closeLists()
      const level = heading[1].length
      const headingId = allocateHeadingId(heading[2], headingIdCounter)
      html += `<h${level} id="${headingId}">${parseInline(heading[2])}</h${level}>`
      continue
    }

    if (/^(-{3,}|\*{3,}|_{3,})$/.test(line.trim())) {
      closeLists()
      html += '<hr />'
      continue
    }

    const quote = line.match(/^>\s?(.*)$/)
    if (quote) {
      closeLists()
      html += `<blockquote>${parseInline(quote[1])}</blockquote>`
      continue
    }

    const ul = line.match(/^[-*+]\s+(.+)$/)
    if (ul) {
      if (inOl) {
        html += '</ol>'
        inOl = false
      }
      if (!inUl) {
        html += '<ul>'
        inUl = true
      }
      html += `<li>${parseInline(ul[1])}</li>`
      continue
    }

    const ol = line.match(/^\d+\.\s+(.+)$/)
    if (ol) {
      if (inUl) {
        html += '</ul>'
        inUl = false
      }
      if (!inOl) {
        html += '<ol>'
        inOl = true
      }
      html += `<li>${parseInline(ol[1])}</li>`
      continue
    }

    closeLists()
    html += `<p>${parseInline(line)}</p>`
  }

  if (inCode) {
    html += `<pre><code>${escapeHtml(codeLines.join('\n'))}</code></pre>`
  }

  closeLists()
  return html
}

export function extractMarkdownHeadings(markdown) {
  if (!markdown || !String(markdown).trim()) {
    return []
  }

  const lines = String(markdown).replace(/\r\n?/g, '\n').split('\n')
  const result = []
  let inCode = false
  const headingIdCounter = new Map()

  for (const line of lines) {
    if (line.trim().startsWith('```')) {
      inCode = !inCode
      continue
    }
    if (inCode) {
      continue
    }
    const heading = line.match(/^(#{1,6})\s+(.+)$/)
    if (!heading) {
      continue
    }
    const level = heading[1].length
    const text = normalizeHeadingText(heading[2])
    const id = allocateHeadingId(heading[2], headingIdCounter)
    result.push({ level, text, id })
  }

  return result
}
