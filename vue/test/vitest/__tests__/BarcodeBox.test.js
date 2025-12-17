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

    expect(wrapper.find('.barcode').text()).toContain('1234567890')
  })
})
