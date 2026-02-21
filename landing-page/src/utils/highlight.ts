// Syntax highlighting utilities using Prism.js
import Prism from 'prismjs'

// Import language support
import 'prismjs/components/prism-javascript'
import 'prismjs/components/prism-typescript'
import 'prismjs/components/prism-jsx'
import 'prismjs/components/prism-tsx'
import 'prismjs/components/prism-css'
import 'prismjs/components/prism-json'
import 'prismjs/components/prism-bash'
import 'prismjs/components/prism-sql'

/**
 * Highlight code with Prism.js
 * @param code - The code to highlight
 * @param language - The programming language
 * @returns HTML string with syntax highlighting
 */
export function highlightCode(code: string, language: string = 'javascript'): string {
  try {
    const grammar = Prism.languages[language]
    if (!grammar) {
      console.warn(`Language "${language}" not supported, falling back to plain text`)
      return Prism.util.encode(code) as string
    }
    return Prism.highlight(code, grammar, language)
  } catch (error) {
    console.error('Error highlighting code:', error)
    return Prism.util.encode(code) as string
  }
}

/**
 * Get language class for Prism
 * @param language - The programming language
 * @returns CSS class name
 */
export function getLanguageClass(language: string): string {
  return `language-${language}`
}

/**
 * Copy text to clipboard
 * @param text - Text to copy
 * @returns Success status
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch (error) {
    console.error('Failed to copy to clipboard:', error)
    return false
  }
}
