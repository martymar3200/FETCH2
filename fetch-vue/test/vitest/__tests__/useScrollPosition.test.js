import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useScrollPosition } from '@/composables/useScrollPosition.js'

installQuasarPlugin()

describe('useScrollPositione', () => {
  it('should return the current scroll position of the window', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { scrollPosition, updateScrollPosition } = useScrollPosition()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          scrollPosition,
          updateScrollPosition
        }
      }
    })

    const wrapper = mount(TestComponent)
    await wrapper.vm.updateScrollPosition()

    expect(wrapper.vm.scrollPosition).toBe(0)
  })
})
