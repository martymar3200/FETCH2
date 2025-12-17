import { boot } from 'quasar/wrappers'
import VueHtmlToPaper from '@/plugins/vue-html-to-paper'

export default boot(({ app }) => {
  const options = {
    name: '_blank',
    specs: [
      'fullscreen=yes',
      'titlebar=yes',
      'scrollbars=yes'
    ],
    styles: [],
    timeout: 1000, // default timeout before the print window appears
    autoClose: false, // if false, the window will not close after printing
    windowTitle: window.document.title // override the window title
  }

  app.use(VueHtmlToPaper, options)
})
