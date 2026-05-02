import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import InfoDisplayLayout from '@/components/InfoDisplayLayout.vue'

installQuasarPlugin()

describe('Info Display Layout Component', () => {
  it('should mount a standard detail display layout', () => {
    const wrapper = mount(InfoDisplayLayout)

    expect(wrapper.find('.info-display').exists()).toBe(true)
    expect(wrapper.find('.info-display-details').exists()).toBe(true)
    expect(wrapper.find('.divider').exists()).toBe(true)
  })
})
