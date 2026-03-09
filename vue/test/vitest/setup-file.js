// This file will be run before each test file
import indexeddb from 'fake-indexeddb'
import { createTestingPinia } from '@pinia/testing'
import { vi } from 'vitest'
import { config } from '@vue/test-utils'

// Register Pinia testing environment globally for all test-utils mounts
config.global.plugins = [
  createTestingPinia({
    createSpy: vi.fn,
    initialState: {
      'user-store': {
        userData: {
          user_id: 1,
          username: 'testuser',
          first_name: 'Test',
          last_name: 'User',
          permissions: []
        }
      }
    }
  })
]

// eslint-disable-next-line no-undef
globalThis.indexedDB = indexeddb

// globally mocks the quueue db in indexDb for components that use background sync
let request = indexedDB.open('workbox-background-sync', 1)
request.onupgradeneeded = function () {
  let db = request.result
  let store = db.createObjectStore('requests', { keyPath: 'id' })
  store.createIndex('queueName', 'title', { unique: true })
}
