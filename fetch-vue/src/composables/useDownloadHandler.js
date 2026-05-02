import moment from 'moment'

export function useDownloadHandler () {
  const handleCSVDownload = (fileData, fileName) => {
    const url = window.URL.createObjectURL(new Blob([fileData], { type: 'text/csv' }))

    // Get the current date and time and format as YYYY_MM_DD_HH_MM_SS
    const formattedDate = moment().format().slice(0, 19).replace(/[-T:]/g, '_')
    const link = document.createElement('a')
    link.href = url
    link.download = `${fileName}_${formattedDate}.csv`
    document.body.appendChild(link)
    link.click()

    link.remove()
    window.URL.revokeObjectURL(url)
  }

  return { handleCSVDownload }
}
