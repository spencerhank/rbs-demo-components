/**
 * plugins/index.js
 *
 * Automatically included in `./src/main.js`
 */

// Plugins
import vuetify from './vuetify'
import pinia from '../store'
import router from '../router'
import 'material-design-icons-iconfont/dist/material-design-icons.css'

export function registerPlugins(app) {
  app
    .use(vuetify, {
      iconfont: 'md'
    })
    .use(router)
    .use(pinia)
}
