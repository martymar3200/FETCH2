import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { useAlertPopup } from '@/composables/useAlertPopup.js'

installQuasarPlugin()

describe('useAlertPopup', () => {
  it('should generate and return a list of alerts', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { alerts, handleAlert, clearAlerts } = useAlertPopup()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          alerts,
          handleAlert,
          clearAlerts
        }
      }
    })

    const wrapper = mount(TestComponent)

    // alerts should be empty
    expect(wrapper.vm.alerts.length).toBe(0)

    // an alert was added
    await wrapper.vm.handleAlert({
      type: 'error',
      text: 'This is a user generated error message',
      autoClose: false
    })
    expect(wrapper.vm.alerts.length).toBe(1)

    // alerts were cleared
    await wrapper.vm.clearAlerts()
    expect(wrapper.vm.alerts.length).toBe(0)
  })
})
