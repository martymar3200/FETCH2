import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import SelectInput from '@/components/SelectInput.vue'

installQuasarPlugin()

describe('Select Input Component', () => {
  it('should mount with passed in options', () => {
    const wrapper = mount(SelectInput, {
      global: {
        provide: {
          //If you are using provide/inject
          'get-nested-key-path': 'test-inject-function'
        }
      },
      props: {
        options: [
          {
            id: 1,
            name: 'test 1'
          },
          {
            id: 2,
            name: 'test 2'
          },
          {
            id: 3,
            name: 'test 3'
          }
        ],
        optionValue: 'id',
        optionLabel: 'name'
      }
    })
    expect(wrapper.vm.localOptions).toMatchObject(wrapper.vm.mainProps.options)
  })
})
