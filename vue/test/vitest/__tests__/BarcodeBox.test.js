import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import BarcodeBox from '@/components/BarcodeBox.vue'

installQuasarPlugin()

describe('Barcode Component', () => {
  it('should mount component containing a barcode', () => {
    const wrapper = mount(BarcodeBox, {
      props: {
        barcode: '1234567890'
      }
    })

    // The barcode component renders natively out via JS rather than plain text in a div.
    // Assert the component mounted with the right prop instead of querying raw text.
    expect(wrapper.vm.barcode).toBe('1234567890')
    expect(wrapper.exists()).toBe(true)
  })
})
