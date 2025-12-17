import { ref, onMounted, onUnmounted } from 'vue'

export function useScrollPosition () {

  const scrollPosition = ref(null)

  function updateScrollPosition () {
    scrollPosition.value = window.scrollY
  }

  function scrollToTop () {
    window.scrollTo({
      top: 0,
      left: 0,
      behavior: 'smooth'
    })
  }

  onMounted(() => document.addEventListener('scroll', updateScrollPosition))
  onUnmounted(() => document.removeEventListener('scroll', updateScrollPosition))

  return {
    scrollPosition,
    scrollToTop,
    updateScrollPosition
  }
}
