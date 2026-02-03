import { boot } from 'quasar/wrappers'
import { Notify } from 'quasar'
import { audioAlert } from '@/utils/audio'

export default boot(() => {
  const originalCreate = Notify.create

  Notify.create = (opts) => {
    // Check if opts is just a string (message only), or object
    // If object, check type or color
    if (typeof opts === 'object' && opts !== null) {
      if (
        opts.type === 'negative' ||
                opts.type === 'error' ||
                opts.color === 'negative' ||
                opts.color === 'red'
      ) {
        audioAlert()
      }
    }

    return originalCreate(opts)
  }
})
