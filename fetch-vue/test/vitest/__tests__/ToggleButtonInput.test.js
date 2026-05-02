import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import ToggleButtonInput from '@/components/ToggleButtonInput.vue'

installQuasarPlugin()

describe('Toggle Button Input Component', () => {
  it('should mount with passed in options', () => {
    const wrapper = mount(ToggleButtonInput, {
      global: {
        provide: {
          //If you are using provide/inject
          'get-nested-key-path': 'test-inject-function'
        }
      },
      props: {
        options: [
          {
            label: 'Yes',
            value: true
          },
          {
            label: 'No',
            value: false
          }
        ]
      }
    })

    expect(wrapper.vm.mainProps.options).toMatchObject([
      {
        label: 'Yes',
        value: true
      },
      {
        label: 'No',
        value: false
      }
    ])
  })

  it('should mount a toggle button with a yes and no option', () => {
    const wrapper = mount(ToggleButtonInput, {
      global: {
        provide: {
          //If you are using provide/inject
          'get-nested-key-path': 'test-inject-function'
        }
      },
      props: {
        options: [
          {
            label: 'Yes',
            value: true
          },
          {
            label: 'No',
            value: false
          }
        ]
      }
    })

    expect(wrapper.find('.custom-toggle').text()).toMatch('YesNo')
  })
})
