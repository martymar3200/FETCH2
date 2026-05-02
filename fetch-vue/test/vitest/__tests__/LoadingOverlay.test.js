import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import LoadingOverlay from '@/components/LoadingOverlay.vue'

installQuasarPlugin()

describe('Loading Overlay Component', () => {
  it('should display a spinner/loading icon whenever our app in data loading state', () => {
    const wrapper = mount(LoadingOverlay, {
      props: {
        propLoading: true
      }
    })

    expect(wrapper.find('.overlay').exists()).toBe(true)
  })
})
