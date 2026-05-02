const dotenv = require('dotenv')

let files
files = {
  ...dotenv.config({ path: 'env/.env' }).parsed,
  ...dotenv.config({ path: `env/.env.${process.env.ENVIRONMENT}` }).parsed
}
// if (process.env.ENVIRONMENT == 'local') {
//   // load using parsed env files from the env folder
//   files = {
//     ...dotenv.config({ path: 'env/.env' }).parsed,
//     ...dotenv.config({ path: `env/.env.${process.env.ENVIRONMENT}` }).parsed
//   }
// } else {
//   // load directly from process.env on server
//   files = {
//     VITE_BASE_URL: process.env.VITE_BASE_URL,
//     VITE_API_BASE_URI: process.env.VITE_API_BASE_URI,
//     VITE_INV_SERVCE_API: process.env.VITE_INV_SERVCE_API,
//     VITE_ENV: process.env.VITE_ENV,
//     VITE_TEST: 'env is processing directly from process.env'
//   }
// }


module.exports = () => {
  Object.keys(files, (key) => {
    if (typeof files[key] !== 'string') {
      files[key] = JSON.stringify(files[key])
    }
  })
  return files
}