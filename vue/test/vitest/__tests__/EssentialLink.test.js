import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import EssentialLink from '@/components/EssentialLink.vue'
import { expect } from 'vitest'

installQuasarPlugin()

describe('Essential Link Component', () => {
  it('should mount a custom link component with title', () => {
    const wrapper = mount(EssentialLink, {
      props: {
        title: 'test link'
      }
    })

    expect(wrapper.find('.essential-link').text()).toMatch('test link')
  })
})
