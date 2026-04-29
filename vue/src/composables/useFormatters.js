import moment from 'moment'

export function useFormatters () {
  const getNestedKeyPath = (obj, path) => {
    if (typeof path === 'string') {
      path = path.replace('?', '').split('.')
    }

    if (path.length === 0) {
      return obj
    }
    return getNestedKeyPath(obj[path[0]], path.slice(1))
  }

  const getUniqueListByKey = (arr, key) => {
    // removes duplicate objects from provided array using specified key
    return arr.filter((obj1, i, array) =>
      array.findIndex(obj2 => (obj2[key] == obj1[key])) == i
    )
  }

  const currentIsoDate = () => {
    const timezoneAwareDateIso = moment().format()
    return timezoneAwareDateIso
  }

  const formatDateTime = (dateTime) => {
    if (!dateTime) {
      return {
        date: '',
        time: '',
        dateTime: ''
      }
    }

    //check if the passed in dateTime has missing timezone offset or Z in the ISO string add the z if not
    if (dateTime && /([zZ]|([+-]\d{2}:?\d{2}))$/.test(dateTime) == false) {
      dateTime = dateTime + 'Z'
    }

    const localTimeFormat = new Date(dateTime).toLocaleString()
    const splitDateTime = localTimeFormat.split(',')
    return {
      date: splitDateTime[0],
      time: splitDateTime[1],
      dateTime: localTimeFormat
    }
  }

  return {
    getNestedKeyPath,
    getUniqueListByKey,
    currentIsoDate,
    formatDateTime
  }
}
