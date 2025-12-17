
// A Custom Fork of the Vue-Html-To-Paper Code and adds composition api support
function addStyles (win, styles) {
  styles.forEach((style) => {
    let link = win.document.createElement('link')
    link.setAttribute('rel', 'stylesheet')
    link.setAttribute('type', 'text/css')
    link.setAttribute('href', style)
    win.document.getElementsByTagName('head')[0].appendChild(link)
  })
}

const VueHtmlToPaper = {
  install (app, options = {}) {
    const htmlToPaper = (
      el,
      localOptions,
      cb = () => true
    ) => {
      let defaultName = '_blank',
        defaultSpecs = [
          'fullscreen=yes',
          'titlebar=yes',
          'scrollbars=yes'
        ],
        defaultReplace = true,
        defaultStyles = [],
        defaultWindowTitle = window.document.title
      let {
        name = defaultName,
        specs = defaultSpecs,
        replace = defaultReplace,
        styles = defaultStyles,
        autoClose = true,
        windowTitle = defaultWindowTitle
      } = options

      // If has localOptions
      if (localOptions) {
        if (localOptions.name) {
          name = localOptions.name
        }
        if (localOptions.specs) {
          specs = localOptions.specs
        }
        if (localOptions.replace) {
          replace = localOptions.replace
        }
        if (localOptions.styles) {
          styles = localOptions.styles
        }
        if (localOptions.autoClose === false) {
          autoClose = localOptions.autoClose
        }
        if (localOptions.windowTitle) {
          windowTitle = localOptions.windowTitle
        }
      }

      specs = specs.length ? specs.join(',') : ''

      const element = window.document.getElementById(el)

      if (!element) {
        alert(`Element to print #${el} not found!`)
        return
      }

      const url = ''
      const win = window.open(url, name, specs, replace)

      win.document.write(`
        <html>
          <head>
            ${localOptions.css ? localOptions.css : ''}
            <title>${windowTitle || window.document.title}</title>
          </head>
          <body>
            ${element.innerHTML}
          </body>
        </html>
      `)

      addStyles(win, styles)

      setTimeout(() => {
        win.focus()
        win.print()
        console.warn('autoClose', autoClose)
        if (autoClose) {
          setTimeout(function () {
            win.close()
          }, 1)
        }
        cb()
      }, 1000)

      return true
    }

    if (app.prototype) {
      app.prototype.$htmlToPaper = htmlToPaper
    } else {
      app.provide('htmlToPaper', htmlToPaper)

      app.config.globalProperties.$htmlToPaper = htmlToPaper
    }
  }
}

export default VueHtmlToPaper