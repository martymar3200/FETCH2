import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import { usePermissionHandler } from '@/composables/usePermissionHandler.js'

installQuasarPlugin()

describe('usePermissionHandlere', () => {
  it('should return a boolean value when passing in user permission strings', async () => {
    // create a mock component to use the composable on
    const TestComponent = defineComponent({
      setup () {
        const { checkUserPermission } = usePermissionHandler()
        return {
          // Call the composable and expose all return values into our
          // component instance so we can access them with wrapper.vm
          checkUserPermission
        }
      }
    })

    const wrapper = mount(TestComponent)
    expect(wrapper.vm.checkUserPermission('fakePermission')).toBe(false)
  })
})
