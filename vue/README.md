_At this time, LC does not have the resources to offer support for this open source code. While LC will make the code available, the Library does not currently promise to address any issues which are pointed out by the community beyond what is needed for the Library's own usage._

# Fetch App

Inventory Management Software Development PWA built with Vue 3 / Pinia State Management / Quasar Framework

See [Vue 3 Docs](https://vuejs.org/guide/introduction.html)

See [Quasar Framework](https://quasar.dev/docs)

See [Pinia Docs](https://pinia.vuejs.org/getting-started.html)

&nbsp;

# Getting Started

### Main File Structure Overview

- . : Configuration files
- package.json: base project settings and packages
- quasar. : files related to quasar configuration/plugins
- vitest. : files for vitest configuration
- dist/ : Build files related to deployment
- env/ : Environment variable files and Environment Parse _(only an example file exists here since we handle this in CI/CD Pipelines)_
- images/ : Container Files to build the application
- nginx/ : Handles ssl and proxying needs for deployment
- public/ : Static files or files that dont change often
- test/ : Contains (unit) tests
- src/ : FETCH Frontend Source Code
- src-pwa/ : Service Worker Setup Files and Manifest

## 1. Installing the application

You can either install the application using the quick install (uses podman and brew) or the manual install guides.

### Quick Installation (Uses a containerized version of the app)

You will need podman and brew installed for this version to work on your pc.

Head to the [fetch-local](https://git.example.com/fetch/fetch-local) repo and follow instructions up to the 'run' step to get a fully working FETCH Application.

_This version will run in pwa mode by default if you need spa mode manual installation is recommended._

### Manual installation Guide

**1. Clone the repository into your local branch using git (make sure you have ssh access beforehand)**

```bash
git clone ssh://git@git.example.com:7999/fetch/vue.git
```

**2. Install the dependencies**

```bash
yarn
# or
npm install
```

**3. Start the app in your desired development mode (SPA, PWA, Local env, Dev env, etc.)**

```bash
# start with either quasar or npm followed by dev and the environment you want to run

# spa mode
quasar dev:local
# or
npm run dev:local

# pwa mode (see extra cert instruction below for to run https locally)
quasar dev:local-pwa
# or
npm run dev:local-pwa

```

**3.1 Successfully running PWA mode with https via localhost**

When running the application in PWA mode the app will launch using the vite devServers https mode which requires a self generated certificate. This is handled locally using the npm command `npm run generate-cert` at the vue root directory (this does require you to have the mkcert package installed and setup on your machine) this will generate a .cert folder which is refrenced in the quasar.config.js devServer setup (it will fail to run in pwa mode without this).

If you are a mac user this should all work right out of the box, if anything you just need to make sure to add the certificate to your trust root certificates if it did not do so automatically when you installed mkcert.

If you are a windows user and using wsl there is a little more extra steps inorder to add the ceritficate to your local system. After you ran `mkcert -install` from the wsl terminal this generates the ceriticate in wsl only, so you need to copy this certificate which can be found in your linux (wsl) files under `/usr/local/share/ca-certificates/mkcert_development_CA_XXXXXXX` to anywhere on your local desktop. Now double click and install the cert to your Trusted Root Certification Authorities and you are now good to go!

Alternative solution to running pwa mode with service workers is to launch chrome via terminal using the following command:

```bash
open -a /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --args --ignore-certificate-errors
```

## 2. Creating new components or views

This application is built using Vue 3's composition api, which gets rid of a lot of the organization that the options api uses. In order to prevent disorganized code structure and make files easy to follow. We have a template file to be used as a starting point for any new views or components. This template file has defined commented code blocks to be used as guidlines for where certain code or logic belongs.

**The template file can be found in the _src/pages/example_ folder under the name _StarterTemplate.vue_**

## 3. Handling API Calls

_If running the local environment make sure you have a .env.local file created under the env folder and that is where you'll define your local api url along with any other variables we have access to which can be found in the env.exmaple file_

### Adding and Referencing api calls

Api calls can be added and referenced under the _src/http_ folder where you will see a js file named after the api its endpoints will be under (ex: inventoryService.js)

### Sending api requests

All api requests are stored and called from a related pinia store file, these are found under _src/stores_.

When adding api calls try to follow the current naming conventions (naming your action function starting with the api request type ex: postTrayItems, patchTrayItems, getTrayItems, deleteTrayItems, ect)

_Please do not call api requests from a component/view_

## 4. Handling Users/Group Permissions

_The Permissions for the FETCH application belong to groups which are created via the admin dashboard. These groups have permissions assigned to them and when a user belongs to that group they recieve that groups permissions. \*Side note: users can belong to multiple groups so they will inherit permissions from all groups that the user belongs to._

### Setting Up Route Blocking/Navigation Guards For Permissions

A Navigational guard function is setup in the _src/router/index.js_ file which blocks routes based on these meta tags: 'requiresAuth' or 'requiresPerm'. The meta tags are setup in the _src/router/routes.js_ file. If a route 'requiresAuth' this means a user must be logged in to the application inorder to view a route with 'requiresAuth' set to true. This is a similar behaviour for 'requiresPerm', except this field takes a string which correllates to permissions set in the api which are assigned through the users groups.

### Handling UI Level Permissions

A composable for handling ui permissions which is found under _src/composables/usePermissionHandler.js_. was created to make checking permissions simple and efficient. All you have to do is import the composable and the function `checkUserPermissions('api_string_permission_here')` to your component/page and set it up where you need to enforce any permissions.

## 5. Linting files (this is automatically handled during pre-commit)

Make sure you have pre-commit installed or the auto linting wont work.
To install pre-commit run the following:

```bash
brew install pre-commit
# then run this at the vue root folder
pre-commit install
```

If you'd like to manually lint and format you can use these commands.

**Lint the files (uses eslint ruleset along with vue standard rules)**

```bash
npm run lint
```

**Lint and Format the files**

```bash
npm run lint:fix
```

## 6. Unit Testing

Unit tests are located in the test/vitest folder and are required anytime you create a new composable or global component. Dont worry about testing views or view specific components as these are not isolated pieces of code and do not require unit tests.

### Run unit tests using the following commands

```bash
# runs a continuous testing suite with a watcher
npm run test:unit

# or for one time tests
npm run test:unit:ci
```

## 7. Building and Testing The PWA App On Mobile / Desktop

The best way to handle testing the PWA portion of the application will require an external hosting server/service which is needed so our dev url can have an ssl cert (pwa's can only run on https). For this guide we will use ngrok. (there are other services and ways which can be found in quasar's documentation link below)

### 1. Run the local build script

```bash
npm run build:local
```

This will build the app in pwa mode and store the built files under ./dist/pwa

### 2. Serve the built files using quasars CLI

```bash
quasar serve ./dist/pwa/ --history
```

This should start a local server at http://127.0.0.1:4000 (sometimes the pwa will load on desktop with just this url even though its not https, however to load the app on mobile we still need an external url)

### 3. Start Ngrok and create a proxy url at port 4000 or whichever port your local server is using

```bash
ngrok http 4000
```

You should now see a proxy url generated in ngrok which should look something like the following:
'https://b92a-47-221-215-243.ngrok-free.app'

Using that url you can now load the application on mobile or desktop and pwa service workers should be active and running.

You should now be able to test out any pwa related functionality within the application!

Now if you need to test local api connection, this is a bit more of a process since you now have to expose your api's localhost as well, one way to do this is to use [adb tools](https://www.xda-developers.com/install-adb-windows-macos-linux/) and expose your computers localhost ports to anything on your network using the following command

```bash
// change the 8001 portion to whatever your local api host is using
adb reverse tcp:8001 tcp:8001
```

you should now be able to access localhost:8001 directly from your mobile devices web browser and be able to interact with the local api!

**Helpful Related Quasar PWA Links:**

See [Exposing Dev Server to Public](https://quasar.dev/quasar-cli-vite/opening-dev-server-to-public/).

See [Quasar PWA With Vite](https://quasar.dev/quasar-cli-vite/developing-pwa/introduction).

**Other Helpful App Related Links:**

See [Vitest](https://vitest.dev/guide/).
See [Vue-Html-To-Paper](https://www.npmjs.com/package/vue-html-to-paper).
See [Vue-Json-Excel3](https://www.npmjs.com/package/vue-json-excel3?activeTab=readme).
