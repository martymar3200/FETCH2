import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useBarcodeScanHandler } from '@/composables/useBarcodeScanHandler.js'

installQuasarPlugin()

describe('useBarcodeScanHandlere', () => {
  it('should return a scanned barcode when scan keyboard event is detected', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { compiledBarCode } = useBarcodeScanHandler()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          compiledBarCode
        }
      }
    })

    const wrapper = mount(TestComponent)

    await wrapper.trigger('keyup', {
      key: 'a'
    })
    await wrapper.trigger('keyup', {
      key: 'b'
    })
    await wrapper.trigger('keyup', {
      key: '!'
    })

    expect(wrapper.vm.compiledBarCode).toBe('')
  })
})
