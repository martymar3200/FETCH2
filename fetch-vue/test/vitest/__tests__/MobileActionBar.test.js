import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import MobileActionBar from '@/components/MobileActionBar.vue'

installQuasarPlugin()

describe('Mobile Action Bar Component', () => {
  it('should mount a fixed bar with cancel and submit buttons', () => {
    const wrapper = mount(MobileActionBar, {
      props: {
        buttonOneLabel: 'Cancel',
        buttonTwoLabel: 'Submit'
      }
    })

    expect(wrapper.find('.mobile-actions').text()).toContain('Cancel')
    expect(wrapper.find('.mobile-actions').text()).toContain('Submit')
  })
})
