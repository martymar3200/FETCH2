/* eslint-env node */

/*
 * This file runs in a Node context (it's NOT transpiled by Babel), so use only
 * the ES6 features that are supported by your Node version. https://node.green/
 */

// Configuration for your app
// https://v2.quasar.dev/quasar-cli-vite/quasar-config-js
const defineEnv = require('./env/envparser')()
const { configure } = require('quasar/wrappers')
const path = require('path')
// const fs = require('fs')


module.exports = configure(function (ctx) {
  return {
    eslint: {
      // include: [],
      // exclude: [],
      warnings: true,
      errors: true
    },

    // https://v2.quasar.dev/quasar-cli/prefetch-feature
    preFetch: true,

    // app boot file (/src/boot) (this is your main.js)
    // --> boot files are generated into a "main.js" file
    // https://v2.quasar.dev/quasar-cli/boot-files
    boot: [
      'axios',
      'htmlToPaper',
      'vueJsonExcel'
    ],

    // https://v2.quasar.dev/quasar-cli-vite/quasar-config-js#css
    css: ['app.scss'],

    // https://github.com/quasarframework/quasar/tree/dev/extras
    // setup icons packs, external fonts and other external plugins here
    extras: [
      // 'ionicons-v4',
      // 'mdi-v5',
      // 'fontawesome-v6',
      // 'eva-icons',
      // 'themify',
      // 'line-awesome',
      // 'roboto-font-latin-ext', // this or either 'roboto-font', NEVER both!
      'roboto-font', // optional, you are not bound to it
      'material-icons', // optional, you are not bound to it
      'mdi-v7'
    ],

    // Full list of options: https://v2.quasar.dev/quasar-cli-vite/quasar-config-js#devServer
    devServer: {
      open: true, // opens browser window automatically
      port: ctx.mode.spa ? 3000 : 8000,
      https: ctx.mode.spa ? false : true
      // https: ctx.mode.spa ? false : {
      //   key: fs.readFileSync('./.cert/key.pem'),
      //   cert: fs.readFileSync('./.cert/cert.pem')
      // }
    },

    // https://v2.quasar.dev/quasar-cli-vite/quasar-config-js#framework
    framework: {
      config: {},
      cssAddon: true,
      iconSet: 'material-icons', // Quasar icon set
      lang: 'en-US', // Quasar language pack

      // For special cases outside of where the auto-import strategy can have an impact
      // (like functional components as one of the examples),
      // you can manually specify Quasar components/directives to be available everywhere:
      //
      // components: [],
      // directives: [],

      // Quasar plugins
      plugins: [
        'LocalStorage',
        'SessionStorage',
        'Notify'
      ]
    },

    // Full list of options: https://v2.quasar.dev/quasar-cli-vite/quasar-config-js#build
    build: {
      target: {
        browser: [
          'es2019',
          'edge88',
          'firefox78',
          'chrome87',
          'safari13.1'
        ],
        node: 'node16'
      },
      alias: {
        '@': path.resolve(__dirname, './src')
      },
      vueRouterMode: 'history', // available values: 'hash', 'history'
      vueOptionsAPI: true,
      publicPath: process.env.VITE_BASE_URL,
      env: defineEnv
      // vueDevtools,
      // rebuildCache: true, // rebuilds Vite/linter/etc cache on startup
      // analyze: true,
      // rawDefine: {}
      // ignorePublicFolder: true,
      // minify: false,
      // polyfillModulePreload: true,
      // distDir
      // extendViteConf (viteConf) {},
      // viteVuePluginOptions: {},
      // vitePlugins: [
      //   [ 'package-name', { ..options.. } ]
      // ]
    },

    // https://v2.quasar.dev/quasar-cli/developing-pwa/configuring-pwa
    pwa: {
      workboxMode: 'injectManifest', // or 'generateSW'
      injectPwaMetaTags: true,
      swFilename: 'sw.js',
      manifestFilename: 'manifest.json',
      useCredentialsForManifestTag: false,
      useFilenameHashes: false,
      extendManifestJson (json) {
        // tamper with the json inline
        json.name = process.env.VITE_ENV == 'production' ? json.name : `${json.name}-${process.env.VITE_ENV}`,
        json.short_name = process.env.VITE_ENV == 'production' ? json.short_name : `${json.short_name}-${process.env.VITE_ENV}`
      }
      // extendInjectManifestOptions (cfg) {},
      // extendPWACustomSWConf (esbuildConf) {}
    },
    sourceFiles: {
      pwaRegisterServiceWorker: 'src-pwa/register-service-worker',
      pwaServiceWorker: 'src-pwa/custom-service-worker', // only used if workboxMode is injectManifest
      pwaManifestFile: 'src-pwa/manifest.json'
    }
  }
})
