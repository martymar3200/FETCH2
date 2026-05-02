import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { DOMWrapper, mount } from '@vue/test-utils'
import PopupModal from '@/components/PopupModal.vue'

installQuasarPlugin()

describe('Popup Modal Component', () => {
  beforeEach(() => {
    mount(PopupModal, {
      props: {
        title: 'Hello',
        text: 'this is a modal'
      }
    })
  })

  it('should mount component with passed in title and text', () => {
    const documentWrapper = new DOMWrapper(document.body)

    expect(documentWrapper.find('.popup-modal').exists()).toBe(true)
    expect(documentWrapper.find('.popup-modal h2').text()).toMatch('Hello')
    expect(documentWrapper.find('.popup-modal').text()).toContain('this is a modal')
  })
})
