import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import TextInput from '@/components/TextInput.vue'

installQuasarPlugin()

describe('Text Input Component', () => {
  it('should mount a text input field with passed in placeholder', () => {
    const wrapper = mount(TextInput, {
      props: {
        placeholder: 'this is a text input'
      }
    })

    expect(wrapper.find('.custom-text input').attributes().placeholder).toMatch('this is a text input')
  })
})
