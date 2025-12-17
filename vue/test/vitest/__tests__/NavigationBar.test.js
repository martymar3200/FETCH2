import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, vi } from 'vitest'
import NavigationBar from '@/components/NavigationBar.vue'

installQuasarPlugin()

afterEach(() => {
  vi.clearAllMocks()
})

// mock vue router which is used in the navigation bar component
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
        path: '/',
        hash: '',
        name: '',
        query: ''
      }
    }
  }
})

// mocks the navigator.serviceWorker
Object.defineProperty(global.navigator, 'serviceWorker', {
  value: {
    register: vi.fn()
  }
})

describe('Navigation Bar Component', () => {
  beforeEach(() => {
    // mocks the servieworker message listenter
    navigator.serviceWorker.addEventListener = vi.fn()
  })

  it('should mount component with a top and hidden side nav', async () => {
    const wrapper = mount(NavigationBar, {
      global: {
        provide: {
          //If you are using provide/inject
          'handle-alert': 'test-inject-function'
        }
      }
    })

    expect(wrapper.find('.q-header').exists()).toBe(true)
    expect(wrapper.find('.q-drawer-container').exists()).toBe(false)
  })
})
