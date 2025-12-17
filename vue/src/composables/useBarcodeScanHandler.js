import { ref, nextTick, watch, onBeforeMount } from 'vue'
import { useBarcodeStore } from '@/stores/barcode-store'
import { storeToRefs } from 'pinia'

// MODIFIED: The composable now accepts an 'options' object to allow for different modes.
export function useBarcodeScanHandler (options = {}) {
  // We check for an explicit option to use the 'Enter' key strategy.
  const { waitForEnterKey = false } = options

  const scannedBarCode = ref([])
  const compiledBarCode = ref('')

  const { barcodeScanAllowed, barcodeInputDelay } = storeToRefs(useBarcodeStore())

  // --- Start of Original Timer-Based Debounce Logic (Fallback) ---
  function debounce (callback) {
    let timeoutId = null
    return (...args) => {
      window.clearTimeout(timeoutId)
      timeoutId = window.setTimeout(() => {
        callback(...args)
      }, (barcodeInputDelay.value * 1000))
    }
  }

  const handleBarcodeCompileDebounced = debounce(async () => {
    // if a timeout passes it means the scanner has completed typing a barcode value
    // once scan is complete we render the compiled barcode and reset the barcode scanning state
    compiledBarCode.value = ''
    await nextTick()

    compiledBarCode.value = scannedBarCode.value.join('')
    scannedBarCode.value = []
  })
  // --- End of Original Timer-Based Debounce Logic ---

  // MODIFIED: This function now contains the logic for BOTH modes.
  function barcodeScanEntry (event) {
    if (waitForEnterKey) {
      // --- NEW 'Enter Key' Mode Logic ---
      if (event.key === 'Enter') {
        event.preventDefault()
        if (scannedBarCode.value.length > 0) {
          nextTick().then(() => {
            compiledBarCode.value = scannedBarCode.value.join('')
            scannedBarCode.value = []
          })
        }
        return
      }

      if (event.key.length === 1) {
        scannedBarCode.value.push(event.key)
      }
    } else {
      // --- ORIGINAL 'Debounce' Mode Logic ---
      // If waitForEnterKey is false, we use the original behavior.
      if (event.key.length === 1) { // We still only want to push single characters
        scannedBarCode.value.push(event.key)
        handleBarcodeCompileDebounced()
      }
    }
  }

  watch(barcodeScanAllowed, (val) => {
    if (val == true) {
      document.addEventListener('keypress', barcodeScanEntry)
    } else {
      document.removeEventListener('keypress', barcodeScanEntry)
    }
  })

  onBeforeMount(() => {
    if (barcodeScanAllowed.value) {
      document.addEventListener('keypress', barcodeScanEntry)
    }
  })

  return {
    scannedBarCode,
    compiledBarCode
  }
}