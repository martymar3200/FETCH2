export function useItemDisplay () {
  const getItemLocation = (itemData) => {
    let module = ''
    let aisle = ''
    let side = ''
    let ladder = ''
    let shelf = ''
    let shelfPosition = ''

    let shelfPositionObj = null
    if (itemData) {
      if (itemData.location) {
        shelfPositionObj = itemData
      } else if (itemData.shelf_position) {
        shelfPositionObj = itemData.shelf_position
      } else if (itemData.tray && itemData.tray.shelf_position) {
        shelfPositionObj = itemData.tray.shelf_position
      }
    }

    if (shelfPositionObj && shelfPositionObj.location) {
      const itemLocationValues = shelfPositionObj.location.split('-')
      module = itemLocationValues[1] || ''
      aisle = itemLocationValues[2] || ''
      side = itemLocationValues[3] || ''
      ladder = itemLocationValues[4] || ''
      shelf = itemLocationValues[5] || ''
      shelfPosition = itemLocationValues[6] || ''
      return `${module}-${aisle}-${side == 'Right' ? 'R' : side == 'Left' ? 'L' : side}-${ladder}-${shelf}-${shelfPosition}`.replace('undefined-', '')
    }

    return ''
  }

  const renderItemBarcodeDisplay = (itemData) => {
    // check if item data object contains barcode.value, or withdrawn_barcode.value field
    if (typeof itemData == 'object' && itemData) {
      return itemData.withdrawn_barcode?.value ?? itemData.barcode.value
    } else {
      return ''
    }
  }

  const renderWithdrawnTrayBarcode = (itemData) => {
    // The withdrawn_loc_bcodes are in the form xxxx-yyyy or xxxx
    // Where xxxx is the shelf barcode and yyyy is the tray
    const barcodes = itemData?.withdrawn_loc_bcodes.split('-')
    return barcodes[1] ?? ''
  }

  const renderWithdrawnShelfBarcode = (itemData) => {
    // The withdrawn_loc_bcodes are in the form xxxx-yyyy or xxxx
    // Where xxxx is the shelf barcode and yyyy is the tray
    const barcodes = itemData?.withdrawn_loc_bcodes.split('-')
    return barcodes[0] ?? ''
  }

  const renderWithdrawnItemLocation = (itemData) => {
    return itemData.status === 'Withdrawn' ? itemData?.withdrawn_location : (itemData.tray ? itemData?.tray?.shelf_position?.location : itemData?.shelf_position?.location)
  }

  return {
    getItemLocation,
    renderItemBarcodeDisplay,
    renderWithdrawnTrayBarcode,
    renderWithdrawnShelfBarcode,
    renderWithdrawnItemLocation
  }
}
