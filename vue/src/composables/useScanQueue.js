import { ref, readonly } from 'vue'

/**
 * A composable that queues barcode scans and processes them sequentially.
 * This prevents rapid scans from being concatenated or lost when the
 * previous scan's API call hasn't finished yet.
 *
 * @param {Function} processFn - Async function that processes a single barcode value.
 *   Called with (barcodeValue: string). Errors are caught per-item and do not stop the queue.
 * @returns {{ enqueue: Function, isProcessing: Ref<boolean>, queueLength: Ref<number> }}
 */
export function useScanQueue (processFn) {
  const queue = ref([])
  const isProcessing = ref(false)

  const queueLength = ref(0)

  async function processQueue () {
    if (isProcessing.value) {
      return
    }

    isProcessing.value = true

    while (queue.value.length > 0) {
      const barcode = queue.value.shift()
      queueLength.value = queue.value.length

      try {
        await processFn(barcode)
      } catch (error) {
        // Errors are handled inside processFn via Notify.
        // We continue processing the rest of the queue.
        console.error('[ScanQueue] Error processing barcode:', barcode, error)
      }
    }

    isProcessing.value = false
    queueLength.value = 0
  }

  function enqueue (barcodeValue) {
    if (!barcodeValue || barcodeValue.trim() === '') {
      return
    }

    queue.value.push(barcodeValue.trim())
    queueLength.value = queue.value.length

    // Kick off processing if not already running
    processQueue()
  }

  return {
    enqueue,
    isProcessing: readonly(isProcessing),
    queueLength: readonly(queueLength)
  }
}
