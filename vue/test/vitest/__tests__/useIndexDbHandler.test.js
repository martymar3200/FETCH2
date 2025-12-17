import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useIndexDbHandler } from '@/composables/useIndexDbHandler.js'

installQuasarPlugin()

describe('useIndexDbHandler', () => {
  it('returns data from specified indexDb reference', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { indexDb } = useIndexDbHandler()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          indexDb
        }
      }
    })

    const wrapper = mount(TestComponent)

    // data should be null since no indexDb exists yet
    expect(wrapper.vm.indexDb).toBe(null)
  })
})
