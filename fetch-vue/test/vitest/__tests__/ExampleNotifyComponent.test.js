import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import { vi } from 'vitest'
import { Notify } from 'quasar'
import ExampleNotifyComponent from './demo/ExampleNotifyComponent.vue'

installQuasarPlugin({ plugins: { Notify } })

describe('notify example', () => {
  it('should call notify on click', async () => {
    expect(ExampleNotifyComponent).toBeTruthy()

    const wrapper = mount(ExampleNotifyComponent, {})
    const spy = vi.spyOn(Notify, 'create')
    expect(spy).not.toHaveBeenCalled()
    wrapper.trigger('click')
    expect(spy).toHaveBeenCalled()
  })
})
