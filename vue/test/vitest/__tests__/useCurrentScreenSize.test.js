import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

installQuasarPlugin()

// create a mock component to use the composable on
const TestComponent = defineComponent({
  setup () {
    const { currentScreenSize } = useCurrentScreenSize()
    return {
      // Call the composable and expose all return values into our
      // component instance so we can access them with wrapper.vm
      currentScreenSize
    }
  }
})

describe('useCurrentScreenSize', () => {
  it('should return the current window size for a user', () => {
    const wrapper = mount(TestComponent)

    expect(wrapper.vm.currentScreenSize).toBe('sm')
  })
})
