// Reuse string context to avoid garbage collection and policy issues
let audioCtx = null

export const audioAlert = () => {
  try {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || window.webkitAudioContext)()
    }

    // Resume context if suspended (common browser policy restriction)
    if (audioCtx.state === 'suspended') {
      audioCtx.resume()
    }

    const oscillator = audioCtx.createOscillator()
    const gainNode = audioCtx.createGain()

    oscillator.connect(gainNode)
    gainNode.connect(audioCtx.destination)

    oscillator.type = 'square'
    oscillator.frequency.value = 280 // Hz
    gainNode.gain.value = 0.1 // 10% volume

    oscillator.start()
    const duration = 0.25 // seconds
    oscillator.stop(audioCtx.currentTime + duration)
  } catch (e) {
    console.warn('Audio alert failed:', e)
  }
}
