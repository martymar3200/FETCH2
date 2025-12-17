import { useQuasar } from 'quasar'
import { computed } from 'vue'

export function useCurrentScreenSize () {
  const $q = useQuasar()

  const currentScreenSize = computed(() => {
    let breakpoint = null
    switch (true) {
      case ($q.screen.width <= 600):
        breakpoint = 'xs'
        break
      case ($q.screen.width > 600 && $q.screen.width <= 1024):
        breakpoint = 'sm'
        break
      case ($q.screen.width > 1024 && $q.screen.width <= 1440):
        breakpoint = 'md'
        break
      case ($q.screen.width > 1440 && $q.screen.width <= 1920):
        breakpoint = 'lg'
        break
      case ($q.screen.width > 1920):
        breakpoint = 'xl'
        break
    }

    return breakpoint
  })

  return {
    currentScreenSize
  }
}
