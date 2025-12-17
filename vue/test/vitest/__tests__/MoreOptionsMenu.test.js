import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import { expect } from 'vitest'

installQuasarPlugin()

describe('More Options Menu Component', () => {
  it('should mount a custom three dot menu component with passed in options', async () => {
    const wrapper = mount(MoreOptionsMenu, {
      props: {
        options: [
          {
            text: 'Test Option 1',
            value: 1
          },
          {
            text: 'Test Option 2',
            value: 2
          }
        ]
      }
    })

    await wrapper.find('.more-menu').trigger('click')

    // check that three button icon exists
    expect(wrapper.find('.more-menu').text()).toMatch('more_vert')

    // check that the options are loaded by the component
    expect(wrapper.vm.options).toMatchObject([
      {
        text: 'Test Option 1',
        value: 1
      },
      {
        text: 'Test Option 2',
        value: 2
      }
    ])
  })
})
