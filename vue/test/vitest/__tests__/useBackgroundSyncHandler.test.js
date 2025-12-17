import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useBackgroundSyncHandler } from '@/composables/useBackgroundSyncHandler.js'

installQuasarPlugin()

describe('useBackgroundSyncHandlere', () => {
  it('returns the queue data from background sync service worker', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { bgSyncData } = useBackgroundSyncHandler()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          bgSyncData
        }
      }
    })

    const wrapper = mount(TestComponent)

    // data should be null since no queued requests exist
    expect(wrapper.vm.bgSyncData).toBe(null)
  })
})
