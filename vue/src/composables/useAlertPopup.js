import { ref } from 'vue'

export function useAlertPopup () {

  const alerts = ref([])

  function handleAlert (options) {
    const defaultOptions = {
      type: 'error',
      text: 'No Message Provided!',
      autoClose: false,
      persistent: false,
      timestamp: Date.now()
    }
    alerts.value.push({
      ...defaultOptions,
      ...options
    })

    // intercept any error based alerts and check if there is an error object and change it to return response data
    if (alerts.value.some(alrt => alrt.type == 'error')) {
      alerts.value.forEach(alrt => {
        if (alrt.type == 'error' && typeof alrt.text == 'object') {
          alrt.text = alrt.text.response?.data ? alrt.text.response.data.detail : alrt.text
        }
      })
    }

    // if an alert has autoClose flag when called clear the alert after 5 seconds
    if (options.autoClose) {
      clearAlerts(defaultOptions.timestamp)
    }
  }

  function clearAlerts (alertTimestamp, forceClear) {
    if (alertTimestamp && !forceClear) {
      // clears out any alerts with the passed in timestamp
      setTimeout(() => {
        alerts.value = alerts.value.filter(alert => {
          if (!alert.autoClose) {
            return alert
          } else if (alert.timestamp !== alertTimestamp) {
            return alert
          }
        })
      }, 5000)
    } else if (alertTimestamp && forceClear) {
      alerts.value = alerts.value.filter(alert => alert.timestamp !== alertTimestamp)
    } else {
      //clear all alerts if no specified alert timestamp is passed
      alerts.value = []
    }
  }

  return {
    alerts,
    handleAlert,
    clearAlerts
  }
}
