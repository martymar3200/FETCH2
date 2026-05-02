import { boot } from 'quasar/wrappers'
import JsonExcel from 'vue-json-excel3'

export default boot(({ app }) => {
  // mounts the vue-json-excel3 package as a reusable global component in our app
  // see README.md link to repo for more information
  app.component('DownloadExcel', JsonExcel)
})