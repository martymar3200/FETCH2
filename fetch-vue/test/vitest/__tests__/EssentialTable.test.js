import { installQuasarPlugin } from '@quasar/quasar-app-extension-testing-unit-vitest'
import { mount } from '@vue/test-utils'
import EssentialTable from '@/components/EssentialTable.vue'
import { expect } from 'vitest'

installQuasarPlugin()

describe('Essential Table Component', () => {
  it('should mount table component with defined columns and data and no table rearrangingr', () => {
    const wrapper = mount(EssentialTable, {
      props: {
        hideTableRearrange: true,
        tableColumns: [
          {
            name: 'id',
            field: 'id',
            label: 'Barcode',
            align: 'left',
            sortable: true
          },
          {
            name: 'media_type',
            field: 'media_type',
            label: 'Media Type',
            align: 'left',
            sortable: true
          },
          {
            name: 'size',
            field: 'size',
            label: 'Size Class',
            align: 'left',
            sortable: true
          }
        ],
        tableData: [
          {
            id: '00924891289',
            media_type: 'Test Material',
            size: 'C Low'
          },
          {
            id: '00924891290',
            media_type: 'Archival Material',
            size: 'C High'
          }
        ]
      }
    })
    expect(wrapper.vm.localTableColumns).toMatchObject(wrapper.vm.mainProps.tableColumns)
    expect(wrapper.find('.table-component-rearrange').exists()).toBe(false)
    expect(wrapper.find('.table-component-table').exists()).toBe(true)
  })

  it('should mount table component with table rearranging but no filtering', () => {
    const wrapper = mount(EssentialTable, {
      props: {
        tableColumns: [
          {
            name: 'id',
            field: 'id',
            label: 'Barcode',
            align: 'left',
            sortable: true
          },
          {
            name: 'media_type',
            field: 'media_type',
            label: 'Media Type',
            align: 'left',
            sortable: true
          },
          {
            name: 'size',
            field: 'size',
            label: 'Size Class',
            align: 'left',
            sortable: true
          }
        ],
        tableData: [
          {
            id: '00924891289',
            media_type: 'Test Material',
            size: 'C Low'
          },
          {
            id: '00924891290',
            media_type: 'Archival Material',
            size: 'C High'
          }
        ]
      }
    })

    expect(wrapper.find('.table-component-rearrange').exists()).toBe(true)
    expect(wrapper.find('.table-component-filter').exists()).toBe(false)
  })

  it('should mount table component with a table filtering', () => {
    const wrapper = mount(EssentialTable, {
      props: {
        tableColumns: [
          {
            name: 'id',
            field: 'id',
            label: 'Barcode',
            align: 'left',
            sortable: true
          },
          {
            name: 'media_type',
            field: 'media_type',
            label: 'Media Type',
            align: 'left',
            sortable: true
          },
          {
            name: 'size',
            field: 'size',
            label: 'Size Class',
            align: 'left',
            sortable: true
          }
        ],
        tableData: [
          {
            id: '00924891289',
            media_type: 'Test Material',
            size: 'C Low'
          },
          {
            id: '00924891290',
            media_type: 'Archival Material',
            size: 'C High'
          }
        ],
        filterOptions: [
          {
            field: 'size',
            options: [
              {
                text: 'C High',
                value: false
              },
              {
                text: 'C Low',
                value: false
              }
            ]
          }
        ]
      }
    })

    expect(wrapper.find('.table-component-filter').exists()).toBe(true)
  })
})
