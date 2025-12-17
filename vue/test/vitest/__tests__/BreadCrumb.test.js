import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'
import BreadCrumb from '@/components/BreadCrumb.vue'

installQuasarPlugin()

afterEach(() => {
  vi.clearAllMocks()
})

// mock vue router which is used by the breadcrumb component
const mockRoutePush = vi.fn()
vi.mock('vue-router', async () => {
  return {
    RouterView: {},
    useRouter: () => {
      return {
        push: mockRoutePush
      }
    },
    useRoute: () => {
      return {
        fullPath: '/accession',
        hash: '',
        name: ''
      }
    }
  }
})

describe('Bread Crumb Component', () => {
  it('should mount a breadcrumb element', () => {
    const wrapper = mount(BreadCrumb)
    expect(wrapper.find('.breadcrumb').exists()).toBe(true)
  })

  it('navigates the user backwards on click of the breadcrumb buttons'), async () => {
    const wrapper = mount(BreadCrumb)

    await wrapper.find('.q-item').click()

    expect(mockRoutePush).toHaveBeenCalled()
  }
})
