<template>
  <div class="table-component">
    <!-- header section -->
    <div
      class="row"
      :class="headingRowClass !== '' ? headingRowClass : ''"
    >
      <slot name="heading-row" />

      <div
        v-if="localFilterOptions.length > 0"
        class="col-auto"
        :class="headingFilterClass !== '' ? headingFilterClass : ''"
      >
        <q-btn
          flat
          no-caps
          :icon="currentScreenSize == 'xs' ? 'none' : 'mdi-filter'"
          :label="currentScreenSize == 'xs' ? 'Filter' : ''"
          class="table-component-filter"
          :class="currentScreenSize == 'xs' ? 'text-accent' : ''"
          aria-label="tableFilterOptions"
          aria-haspopup="menu"
          :aria-expanded="tableFilterMenuState"
        >
          <q-menu
            :transition-show="currentScreenSize == 'xs' ? 'scale' : 'fade'"
            :transition-hide="currentScreenSize == 'xs' ? 'scale' : 'fade'"
            :class="currentScreenSize == 'xs' ? $style['mobile-menu'] : ''"
            @show="tableFilterMenuState = true"
            @hide="tableFilterMenuState = false"
            aria-label="tableFilterOptionsMenu"
          >
            <q-item-label class="text-h6 q-pa-md">
              Filter Options
            </q-item-label>
            <q-space class="divider" />
            <q-list
              class="table-component-filter-list q-mt-sm"
            >
              <q-item
                v-for="(data, i) in localFilterOptions"
                :key="i"
                role="menuitem"
              >
                <q-item-section>
                  <q-item-label header>
                    {{ data.label }}
                  </q-item-label>

                  <template v-if="data.options.length > 0">
                    <q-item
                      v-for="opt in data.options"
                      :key="opt.text"
                      tag="label"
                      v-ripple
                      :class="opt.value ? 'active' : ''"
                      role=""
                    >
                      <q-item-section
                        side
                        top
                      >
                        <q-checkbox
                          v-model="opt.value"
                          @update:model-value="filterTableData(opt)"
                          aria-label="tableFilterOptionCheckbox"
                          role="menuitemcheckbox"
                        />
                      </q-item-section>

                      <q-item-section>
                        <q-item-label>{{ opt.text }}</q-item-label>
                      </q-item-section>
                    </q-item>
                  </template>
                  <q-item
                    v-else
                    :disable="true"
                    tag="label"
                    role=""
                  >
                    <q-item-section>
                      <q-item-label>No Filters Found</q-item-label>
                    </q-item-section>
                  </q-item>
                </q-item-section>
              </q-item>
            </q-list>
          </q-menu>
        </q-btn>
      </div>

      <div
        v-if="!hideTableRearrange"
        class="col-auto"
        :class="headingRearrangeClass !== '' ? headingRearrangeClass : ''"
      >
        <q-select
          ref="tableSortFilter"
          outlined
          multiple
          :dense="currentScreenSize == 'xs'"
          aria-label="tableRearrangeMenu"
          :display-value="'Rearrange'"
          v-model="localTableVisibleColumns"
          :options="localTableColumns.filter(opt => !opt.required)"
          emit-value
          map-options
          use-input
          option-value="name"
          option-label="label"
          class="table-component-rearrange full-width"
          :popup-content-class="$style['rearrange-menu']"
          @popup-hide="allowTableReorder = false"
        >
          <template
            v-if="enableTableReorder"
            #before-options
          >
            <q-item>
              <q-item-section>
                <q-item-label>Rearrange Columns</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-toggle v-model="allowTableReorder" />
              </q-item-section>
            </q-item>

            <q-space class="divider" />
          </template>

          <template #option="{ itemProps, opt, selected, toggleOption }">
            <q-item
              v-bind="itemProps"
              :style="{'order': opt.order}"
              :draggable="allowTableReorder"
              @dragstart="startDrag($event)"
              @dragend="endDrag($event)"
              @dragover="reorderTableItemDOM($event)"
            >
              <q-item-section
                v-if="allowTableReorder"
                side
              >
                <q-icon
                  name="drag_indicator"
                  color="primary"
                  size="25px"
                />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ opt.label }}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-checkbox
                  :model-value="selected"
                  @update:model-value="toggleOption(opt)"
                />
              </q-item-section>
            </q-item>
          </template>
        </q-select>
      </div>
    </div>

    <!-- main table section -->
    <div class="row">
      <div class="col-grow">
        <q-table
          ref="tableComponent"
          flat
          :dense="currentScreenSize == 'xs'"
          :rows="localTableData"
          :columns="allowTableReorder ? localTableColumns.map(item => ({...item, sortable: false})) : localTableColumns.map(item => ({...item, sortable: item.sortable}))"
          :visible-columns="localTableVisibleColumns"
          :row-key="rowKey"
          :wrap-cells="true"
          :hide-selected-banner="true"
          column-sort-order="da"
          :hide-pagination="enablePagination ? false : true"
          v-model:pagination="paginationConfig"
          :loading="paginationLoading"
          :rows-per-page-options="rowsPerPageOptions"
          :selection="enableSelection ? 'multiple' : 'none'"
          v-model:selected="selectedTableData"
          @request="onTableRequest"
          class="table-component-table"
          tabindex="0"
        >
          <template #header-cell-actions="props">
            <!-- if we ever pass in an actions column it will always use the smallest col size -->
            <q-th
              v-if="props.col.name == 'actions'"
              :auto-width="true"
              style="padding-left: 8px;"
            />
          </template>

          <template #header-selection="scope">
            <q-checkbox
              v-model="scope.selected"
              aria-label="tableSelectAll"
            />
          </template>

          <template #header-cell="props">
            <q-th
              class=""
              :class="props.col.__thClass"
              :style="props.col.headerStyle"
            >
              <span
                :tabindex="props.col.label ? 0 : -1"
                class="flex no-wrap items-center"
                :aria-label="`${props.col.label.replaceAll('#', 'Number')}TableColumnSortAscendDescend`"
                @keydown.enter="props.sort(props.col.name)"
                @click="props.sort(props.col.name)"
              >
                {{ props.col.label }}
                <q-icon
                  name="arrow_upward"
                  class="q-table__sort-icon q-table__sort-icon--left"
                  aria-label="tableColumnSortIcon"
                />
              </span>
            </q-th>
          </template>

          <template #body-selection="scope">
            <q-checkbox
              v-model="scope.selected"
              aria-label="tableRowSelect"
            />
          </template>

          <template #body-cell="props">
            <q-td
              :tabindex="props.value ? 0 : -1"
              :props="props"
              :style="[ props.col.name == 'actions' ? 'padding-left:8px;' : null ]"
              :class="(props.row[highlightRowKey] && props.row[highlightRowKey] == highlightRowValue) ? highlightRowClass : null"
              @click="props.col.name !== 'actions' ? emit('selected-table-row', props.row) : null"
              @keydown.enter="props.col.name !== 'actions' ? emit('selected-table-row', props.row) : null"
            >
              <slot
                name="table-td"
                :props="props"
                :col-name="props.col.name"
                :value="props.value"
                :class="props.col.name == 'actions' ? 'q-pl-none' : null"
              >
                <span>
                  {{ props.value }}
                </span>
              </slot>
            </q-td>
          </template>
        </q-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, toRaw } from 'vue'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

// Props
const mainProps = defineProps({
  enablePagination: {
    type: Boolean,
    default: false
  },
  paginationTotal: {
    type: Number,
    default: 0
  },
  paginationLoading: {
    type: Boolean,
    default: false
  },
  rowsPerPageOptions: {
    type: Array,
    default: () => {
      return [
        25,
        50,
        75,
        100
      ]
    }
  },
  enableSelection: {
    type: Boolean,
    default: false
  },
  hideTableRearrange: {
    type: Boolean,
    default: false
  },
  enableTableReorder: {
    type: Boolean,
    default: false
  },
  tableVisibleColumns: {
    type: Array,
    default () {
      return []
    }
  },
  tableColumns: {
    type: Array,
    default () {
      return []
    },
    required: true
  },
  tableData: {
    type: Array,
    default () {
      return []
    },
    required: true
  },
  filterOptions: {
    type: Array,
    default () {
      return []
      // example of how filterOptions need to be structured
      // [
      //   {
      //     field: 'media_type', or field: row => row.media_type.name
      //     label: 'Media Type'
      //     options: [
      //       {
      //         text: 'Document',
      //         value: false
      //       },
      //       {
      //         text: 'Archival Material',
      //         value: false
      //       }
      //     ]
      //   },
      //   {
      //     field: 'container_type',
      //     label: 'Container Type'
      //     options: [
      //       {
      //         text: 'Book Tray',
      //         value: false
      //       }
      //     ]
      //   },
      //   {
      //     field: 'trayed',
      //     label: 'Trayed'
      //     options: [
      //       {
      //         text: 'Trayed',
      //         boolValue: true,
      //         value: false
      //       },
      //       {
      //         text: 'Non-Trayed',
      //         boolValue: false,
      //         value: false
      //       }
      //     ]
      //   }
      // ]
      // example of how filterOptions need to be structured when filtering via api
      // [
      //   {
      //     apiField: 'media_type'
      //     field: row => row.media_type.name
      //     label: 'Media Type'
      //     options: [
      //       {
      //         text: 'Document',
      //         value: false
      //       },
      //       {
      //         text: 'Archival Material',
      //         value: false
      //       }
      //     ]
      //   }
      // ]
    }
  },
  headingRowClass: {
    type: String,
    default: ''
  },
  headingFilterClass: {
    type: String,
    default: ''
  },
  headingRearrangeClass: {
    type: String,
    default: ''
  },
  rowKey: {
    type: undefined,
    default: 'id' // if tableData doesnt include an 'id' param we need to specifiy this
    // warning some data patterns like tray/non tray based tables could contain similar id's this will cause duplicate issues
  },
  // inorder to generate a class to highlight an entire row of data you must provide a key/value pair to use as an indicator on what row needs the class added
  highlightRowClass: {
    type: String,
    default: ''
  },
  highlightRowKey: {
    type: String,
    default: null
  },
  highlightRowValue: undefined
})

// Emits
const emit = defineEmits([
  'selected-table-row',
  'selected-data',
  'update-pagination'
])

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Local Data
const localTableVisibleColumns = ref(mainProps.tableVisibleColumns)
const localTableColumns = ref(mainProps.tableColumns)
const localTableData = ref(toRaw(mainProps.tableData)) // Creates a copy of the tableData prop so we dont mutate our original prop array when filtering
const localFilterOptions = ref(mainProps.filterOptions)
const allowTableReorder = ref(false)
const draggedItemElement = ref(null)
const selectedTableData = ref([])
const tableComponent = ref(null)
const tableFilterMenuState = ref(false)
const paginationConfig = ref({
  sortBy: '',
  descending: false,
  page: 1,
  rowsPerPage: 50 // matches default of api size of 50
})

// Logic
onMounted(() => {
  if (mainProps.enablePagination == false) {
    // when no pagination is needed we set this value to 0 which defaults to showing all data from localTableData in our table
    paginationConfig.value.rowsPerPage = 0
  }

  // if pagination is enabled and a pagination total is passed in we add the rowsNumber param to our pagination config as needed for server side rendering
  if (mainProps.enablePagination && mainProps.paginationTotal !== 0) {
    paginationConfig.value.rowsNumber = mainProps.paginationTotal
  }
  // if no tableVisibleColumns prop is passed map the tableColumns so all columns are always visible
  if (mainProps.tableVisibleColumns.length == 0) {
    localTableVisibleColumns.value = mainProps.tableColumns.map(col => col.name)
  }
})

watch(selectedTableData, () => {
  emit('selected-data', selectedTableData.value)
})

// update any of the main table column/filter related options if prop data changes
watch(() => mainProps.tableVisibleColumns, (updatedPropData) => {
  localTableVisibleColumns.value = updatedPropData
},
{ deep: true })
watch(() => mainProps.tableColumns, (updatedPropData) => {
  localTableColumns.value = updatedPropData
},
{ deep: true })
watch(() => mainProps.filterOptions, (updatedPropData) => {
  localFilterOptions.value = updatedPropData
},
{ deep: true })

// watch the tableData props for a change update the localTableData with a copy/non reactive clone
watch(() => mainProps.tableData, (updatedTableData) => {
  localTableData.value = toRaw(updatedTableData)
},
{ deep: true })

// watch the paginated related props for changes and update our local paginationConfig
watch(() => mainProps.paginationTotal, () => {
  if (mainProps.enablePagination) {
    paginationConfig.value.rowsNumber = mainProps.paginationTotal
  }
},
{ deep: true })

const clearSelectedData = () => {
  tableComponent.value.clearSelection()
}
const resetTablePagination = () => {
  paginationConfig.value.sortBy = ''
  paginationConfig.value.descending = false
  paginationConfig.value.page = 1
  paginationConfig.value.rowsPerPage = 50
}

const filterTableData = () => {
  // get all user selected filters
  const activeFilters = localFilterOptions.value.flatMap(opt => {
    if (opt.options.some(obj => obj.value == true)) {
      // if opt contains an apiField return that instead of the table field (this is for paginated filtering from the api)
      const optValue = opt.options.filter(obj => obj.value == true).map(obj => Object.hasOwn(obj, 'boolValue') ? obj.boolValue : obj.text)
      if (opt.apiField) {
        return {
          [opt.apiField]: optValue
        }
      } else {
        return {
          [opt.field]: optValue
        }
      }
    } else {
      return []
    }
  })
  // convert the filters array to a single object
  const activeFiltersObj = Object.assign({}, ...activeFilters)

  if (mainProps.enablePagination) {
    // call the table request function and pass active filters + required table props
    onTableRequest(tableComponent.value, activeFiltersObj)
  } else {
    // filters the original table data based on the active filters obj if pagination is not enabled
    const filteredData = mainProps.tableData.filter(entry => {
    // iterate through every active filter by field and selected filter values and check if the value exists under the table data entrys field
      const filterMatchesData = Object.entries(activeFiltersObj).every(([
        field,
        val
      ]) => {
        if (field.includes('=>')) {
          // if we pass in an arrow function string convert it to an actual function to check the entry param path
          // ex row => row.barcode.value becomes entry.barcode.value and we compare that value to the selected filter
          const fieldArrowFunc = eval(field)
          return val.includes(fieldArrowFunc(entry))
        } else {
          return val.includes(entry[field])
        }
      })
      return filterMatchesData
    })

    localTableData.value = [...toRaw(filteredData)]
  }
}


const onTableRequest = (props, tableFilters) => {
  if (mainProps.enablePagination) {
    const { page, sortBy, rowsPerPage, descending } = props.pagination

    // update local pagination object to match table props
    paginationConfig.value = {
      ...paginationConfig.value,
      page,
      sortBy,
      rowsPerPage,
      descending
    }

    // if filters are passed update our pagination object and add/remove the filters
    if (tableFilters) {
      paginationConfig.value = {
        ...tableFilters,
        page,
        sortBy,
        rowsPerPage,
        descending,
        rowsNumber: paginationConfig.value.rowsNumber
      }
    }

    // emit to parent to get next set of paged data with refrenced query params
    emit('update-pagination', {
      ...paginationConfig.value,
      size: paginationConfig.value.rowsPerPage,
      page: paginationConfig.value.page,
      sort_by: paginationConfig.value.sortBy,
      sort_order: paginationConfig.value.descending ? 'desc' : 'asc'
    })
  }
}

const startDrag = (e) => {
  e.target.classList.add('dragging')
  draggedItemElement.value= e.target
}
const endDrag = (e) => {
  e.target.classList.remove('dragging')
  draggedItemElement.value= null

  // get all the child element order values that are in our select filter list
  const filterMenuElements = [...document.querySelector('.q-menu .q-virtual-scroll__content').children].map(el => ({ order: el.style.order }))

  // filter out any required items since these wont show up in the rearrange menu
  const filteredTableColumns = localTableColumns.value.filter(item => !item.required)
  // update the localTableColumn order values to match the filterMenuElements order
  filteredTableColumns.forEach((item, i) => {
    const currentTableColumnIndex = localTableColumns.value.findIndex(currentItm => currentItm == item )
    localTableColumns.value[currentTableColumnIndex].order = filterMenuElements[i].order
  })

  // lastly sort the localTableColumn data which will re render the qtable to the new order
  localTableColumns.value = localTableColumns.value.sort((a, b) => a.order - b.order)
}
const reorderTableItemDOM = (e) => {
  e.preventDefault()

  // swap the hovered child elements style order property with the draggedItemElements style order property
  const hoveredItemElement = e.target.closest('.q-virtual-scroll__content .q-item')
  const currentHoveredOrderValue = hoveredItemElement.style.order
  const draggedItemOrderValue = draggedItemElement.value.style.order
  hoveredItemElement.style.order = draggedItemOrderValue
  draggedItemElement.value.style.order = currentHoveredOrderValue
}


defineExpose({
  clearSelectedData,
  resetTablePagination
})
</script>

<style lang="scss" scoped>
.table-component {
  position: relative;

  &-filter {
    height: 100%;

    @media (max-width: $breakpoint-sm-min) {
      padding: 4px;
      :deep(.q-icon) {
        display: none
      }
    }

    &-list {
      & > .q-item {
        padding: 0;

        & > .q-item__section > .q-item__label {
          padding: 8px 16px;
        }

        & .active {
          color: $primary;
        }
      }
    }
  }

  &-rearrange {
    min-width: 231px;

    @media (max-width: $breakpoint-sm-min) {
      min-width: 100px;
    }

    :deep(.q-field__input) {
      // hides the input from user since our rearrange menu is just a dropdown
      position: absolute;
      outline: 0 !important;
      width: 1px;
      height: 1px;
      padding: 0;
      border: 0;
      opacity: 0;
    }
  }

  &-table {
    :deep(tbody) {
      & tr td {
        cursor: pointer;

        &.q-td--no-hover {
          cursor: initial;
        }
      }
    }
  }
}

.set-focus {
  &:hover {
    cursor: pointer;
  }

  &:focus {
    color: $accent;
    background: rgba($accent, .1);
  }

  &:focus-visible {
    outline: none;
  }
}

.dragging {
  opacity: .4;
}
</style>

<style lang="scss" module>
.rearrange-menu {
  :global(div.q-virtual-scroll__content) {
    display: flex;
    flex-flow: column nowrap;
  }
}

.mobile-menu {
  @media (max-width: $breakpoint-sm-min) {
    top: 50% !important;
    left: 50% !important;
    transform: translate(-50%, -50%);
    min-width: 85vw;
    max-height: 80vh !important;
    box-shadow: 0 0 0 100vh rgba(0,0,0,.4);
  }
}
</style>