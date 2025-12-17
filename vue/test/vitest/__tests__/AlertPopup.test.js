import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import AlertPopup from '@/components/AlertPopup.vue'

installQuasarPlugin()

describe('Alert Popup Component', async () => {
  it('should display an error alert with passed in text', () => {
    const elem = document.createElement('div')
    if (document.body) {
      document.body.appendChild(elem)
    }

    const wrapper = mount(AlertPopup, {
      global: {
        provide: {
          //If you are using provide/inject
          'audio-alert': 'test-inject-function'
        }
      },
      props: {
        alertType: 'error',
        alertText: 'this is a test'
      },
      attachTo: elem
    })

    expect(wrapper.find('.alert-banner').exists()).toBe(true)
    expect(wrapper.find('.alert-banner').text()).toContain('this is a test')
  })
})
