<template>
  <div class="admin-list-manager q-pa-xs-sm q-pa-sm-lg">
    <div class="row q-mb-xs-xl q-mb-sm-none">
      <div class="col-grow q-mb-xs-md q-mb-sm-none">
        <EssentialTable
          :table-columns="listTableColumns"
          :table-visible-columns="listTableVisibleColumns"
          :filter-options="listTableFilters"
          :table-data="listData"
          :enable-table-reorder="false"
          :heading-row-class="'q-mb-xs-md q-mb-md-lg'"
          :heading-filter-class="currentScreenSize == 'xs' ? 'col-xs-6 q-mr-auto' : 'q-ml-auto'"
          :heading-rearrange-class="'q-mr-xs-auto q-mr-sm-none q-ml-sm-auto'"
          :enable-pagination="mainProps.listType == 'shelf-type' ? false: true"
          :pagination-total="listDataTotal"
          :pagination-loading="appIsLoadingData"
          @update-pagination="loadListData($event)"
        >
          <template #heading-row>
            <div
              class="col-xs-12 col-lg-auto q-mr-auto q-pb-xs-sm q-pb-lg-none"
              :class="currentScreenSize == 'xs' ? '' : 'self-center'"
            >
              <h1 class="text-h4 text-bold">
                {{ renderTableTitle }}
              </h1>
            </div>

            <div
              class="col-xs-5 col-sm-auto flex justify-end"
              :class="'order-1'"
            >
              <q-btn
                no-caps
                unelevated
                icon="add"
                color="accent"
                :label="renderTableAction"
                class="btn-no-wrap text-body1 q-ml-sm"
                @click="showListInputModal.type = 'Add'"
              />
            </div>
          </template>

          <template #table-td="{ colName, props }">
            <span
              v-if="colName == 'actions'"
            >
              <MoreOptionsMenu
                :options="generateTableOptionsMenu(props.row)"
                class=""
                @click="handleOptionMenu($event, props.row)"
              />
            </span>
          </template>
        </EssentialTable>
      </div>
    </div>
  </div>

  <!-- add/editlist property modal -->
  <AdminListManagerModal
    v-if="showListInputModal.type !== ''"
    :list-type="listType"
    :action-type="showListInputModal.type"
    :list-data="showListInputModal.listData"
    @hide="showListInputModal.type = ''; showListInputModal.listData = {}"
    @new-list-option-added="listDataTotal++"
  />

  <!-- confirmation modal -->
  <PopupModal
    v-if="showConfirmationModal !== null"
    ref="confirmationModal"
    :title="'Confirm Delete'"
    :text="showConfirmationModal.text"
    :show-actions="false"
    @reset="showConfirmationModal = null"
    aria-label="confirmationModal"
  >
    <template #footer-content="{ hideModal }">
      <q-card-section
        class="row no-wrap justify-between items-center q-pt-sm"
      >
        <q-btn
          no-caps
          unelevated
          color="negative"
          :label="`Delete ${renderTableTitle}`"
          class="btn-no-wrap text-body1 full-width"
          :loading="appActionIsLoadingData"
          @click="deleteListOption(showConfirmationModal.id)"
        />

        <q-space class="q-mx-xs" />

        <q-btn
          outline
          no-caps
          label="Cancel"
          class="text-body1 full-width"
          @click="hideModal"
        />
      </q-card-section>
    </template>
  </PopupModal>
</template>

<script setup>
import { onBeforeMount, ref, inject, computed } from 'vue'
import { useGlobalStore } from '@/stores/global-store'
import { useOptionStore } from '@/stores/option-store'
import { storeToRefs } from 'pinia'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'
import EssentialTable from 'src/components/EssentialTable.vue'
import MoreOptionsMenu from '@/components/MoreOptionsMenu.vue'
import AdminListManagerModal from '@/components/Admin/AdminListManagerModal.vue'
import PopupModal from '@/components/PopupModal.vue'

// Props
const mainProps = defineProps({
  listType: {
    type: String,
    required: true
  }
})

// Composables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  appIsLoadingData,
  appActionIsLoadingData
} = storeToRefs(useGlobalStore())
const {
  getOptions,
  getParentOwnerOptions,
  deleteSizeClass,
  deleteMediaType,
  deleteOwner,
  deleteShelfType
} = useOptionStore()
const {
  optionsTotal,
  owners,
  mediaTypes,
  sizeClass,
  shelfTypes,
  ownersTiers
} = storeToRefs(useOptionStore())

// Local Data
const listDataTotal = ref(0)
const listData = computed(() => {
  let tableData = []
  switch (mainProps.listType) {
    case 'owner':
      tableData = owners.value
      break
    case 'media-type':
      tableData = mediaTypes.value
      break
    case 'size-class':
      tableData = sizeClass.value
      break
    case 'shelf-type': {
    // remove any duplicate shelf types by type name
    // ex: [{ id: 1, type: 'Full' }, { id: 2, type: 'Short' }, { id: 3, type: 'Full' }] returns [{ id: 1, type: 'Full' }, { id: 2, type: 'Short' }]
      const uniqueShelfTypes = shelfTypes.value.reduce ((initArr, current) => {
        const matchingShelfType = initArr.find(st => st.type === current.type)
        if (!matchingShelfType) {
          return initArr.concat([current])
        } else {
          return initArr
        }
      }, [])
      tableData = uniqueShelfTypes
      break
    }
    default:
      break
  }
  return tableData
})
const listTableVisibleColumns = ref([])
const listTableColumns = ref([])
const listTableFilters =  ref([])
const renderTableTitle = computed(() => {
  let title = ''
  if (mainProps.listType == 'owner') {
    title = 'Owner'
  } else if (mainProps.listType == 'media-type') {
    title = 'Media Type'
  } else if (mainProps.listType == 'shelf-type') {
    title = 'Shelf Type'
  } else {
    title = 'Size Class'
  }
  return title
})
const renderTableAction = computed(() => {
  let actionText = ''
  if (mainProps.listType == 'owner') {
    actionText = 'Add Owner'
  } else if (mainProps.listType == 'media-type') {
    actionText = 'Add Media Type'
  } else if (mainProps.listType == 'shelf-type') {
    actionText = 'Add Shelf Type'
  } else {
    actionText = 'Add Size Class'
  }
  return actionText
})
const showListInputModal = ref({
  type: '',
  listType: mainProps.listType,
  listData: {}
})
const confirmationModal = ref(null)
const showConfirmationModal = ref(null)


// Logic
const handleAlert = inject('handle-alert')

onBeforeMount(() => {
  loadListData()
  generateListTableInfo()
})

const handleOptionMenu = async (option, rowData) => {
  // load any options info that will be needed in our modal popup
  if (mainProps.listType == 'size-class') {
    appIsLoadingData.value = true
    await Promise.all([getOptions('owners')])
    appIsLoadingData.value = false
  } else if (mainProps.listType == 'media-type') {
    appIsLoadingData.value = true
    await Promise.all([getOptions('media-types')])
    appIsLoadingData.value = false
  } else if (mainProps.listType == 'shelf-type') {
    appIsLoadingData.value = true
    await Promise.all([getOptions('sizeClass')])
    appIsLoadingData.value = false
  } else if (mainProps.listType == 'owner') {
    appIsLoadingData.value = true
    await Promise.all([getOptions('ownersTiers')])
    // Retrieve filtered list of parent owner options based on the selected owner tier
    let currentTier = ownersTiers.value.find( (ot) => ot.id == rowData.owner_tier_id)
    if (currentTier?.level > 1) {
      await Promise.all([getParentOwnerOptions({ owner_tier_id: ownersTiers.value.find( (ot) => ot.level === currentTier.level - 1)?.id })])
    } else {
      rowData.parent_owner_id = null
    }
    appIsLoadingData.value = false
  }

  if (option.text.includes('Edit')) {
    showListInputModal.value.listData = rowData
    showListInputModal.value.type = 'Edit'
  } else {
    showConfirmationModal.value = {
      text: `Are you sure you want to delete '${rowData.name || rowData.type}'?`,
      id: rowData.id
    }
  }
}

const generateTableOptionsMenu = () => {
  let options = []
  if (mainProps.listType == 'owner') {
    options = [
      { text: 'Edit Owner' },
      {
        text: 'Delete Owner',
        optionClass: 'text-negative'
      }
    ]
  } else if (mainProps.listType == 'media-type') {
    options = [
      { text: 'Edit Media Type' },
      {
        text: 'Delete Media Type',
        optionClass: 'text-negative'
      }
    ]
  } else if (mainProps.listType == 'shelf-type') {
    options = [
      { text: 'Edit Shelf Type' },
      {
        text: 'Delete Shelf Type',
        optionClass: 'text-negative'
      }
    ]
  } else {
    options = [
      { text: 'Edit Size Class' },
      {
        text: 'Delete Size Class',
        optionClass: 'text-negative'
      }
    ]
  }
  return options
}

const generateListTableInfo = () => {
  // creates the report table fields needed based on the selected list type
  switch (mainProps.listType) {
    case 'size-class':
      listTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'name',
          field: 'name',
          label: 'Full Name',
          align: 'left',
          sortable: true
        },
        {
          name: 'short_name',
          field: 'short_name',
          label: 'Short Name',
          align: 'left',
          sortable: true
        },
        {
          name: 'width',
          field: 'width',
          label: 'Width (in)',
          align: 'left',
          sortable: true
        },
        {
          name: 'depth',
          field: 'depth',
          label: 'Depth (in)',
          align: 'left',
          sortable: true
        },
        {
          name: 'height',
          field: 'height',
          label: 'Height (in)',
          align: 'left',
          sortable: true
        }
      ]
      listTableVisibleColumns.value = [
        'actions',
        'name',
        'short_name',
        'width',
        'depth',
        'height'
      ]
      break
    case 'media-type':
      listTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'name',
          field: 'name',
          label: 'Name',
          align: 'left',
          sortable: true
        }
      ]
      listTableVisibleColumns.value = [
        'actions',
        'name'
      ]
      break
    case 'shelf-type':
      listTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'shelf_type',
          field: 'type',
          label: 'Shelf Type',
          align: 'left',
          sortable: true
        }
      ]
      listTableVisibleColumns.value = [
        'actions',
        'shelf_type'
      ]
      break
    case 'owner':
      listTableColumns.value = [
        {
          name: 'actions',
          field: 'actions',
          label: '',
          align: 'center',
          sortable: false,
          required: true
        },
        {
          name: 'name',
          field: 'name',
          label: 'Owner Name',
          align: 'left',
          sortable: true
        },
        {
          name: 'parent_owner_id',
          field: row => row.parent_owner?.name,
          label: 'Parent Owner',
          align: 'left',
          sortable: true
        },
        {
          name: 'owner_tier_id',
          field: 'owner_tier_id',
          label: 'Owner Tier',
          align: 'left',
          sortable: true
        }
      ]
      listTableVisibleColumns.value = [
        'actions',
        'name',
        'parent_owner_id',
        'owner_tier_id'
      ]
      break
    default:
      break
  }
}

const loadListData = async (qParams) => {
  try {
    appIsLoadingData.value = true
    if (mainProps.listType == 'owner') {
      await getOptions('owners', qParams)
    } else if (mainProps.listType == 'media-type') {
      await getOptions('mediaTypes', qParams)
    } else if (mainProps.listType == 'shelf-type') {
      //TEMP loop the shelf types until we get all shelf type data needed for the page display
      await getOptions('shelfTypes', qParams)
      if (optionsTotal.value > 50) {
        let page = 2
        let totalPages = Math.ceil(optionsTotal.value/50)
        while (page <= totalPages) {
          await getOptions('shelfTypes', {
            ...qParams,
            page
          }, true)
          page++
        }
      }
    } else {
      await getOptions('sizeClass', qParams)
    }

    // set the listData total based on the loaded list options from store
    listDataTotal.value = optionsTotal.value
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appIsLoadingData.value = false
  }
}

const deleteListOption = async (id) => {
  try {
    appActionIsLoadingData.value = true
    switch (mainProps.listType) {
      case 'size-class': {
        await deleteSizeClass(id)
        break
      }
      case 'media-type':
        await deleteMediaType(id)
        break
      case 'owner':
        await deleteOwner(id)
        break
      case 'shelf-type': {
        const matchingShelfTypesById = shelfTypes.value.filter(s => s.type == shelfTypes.value.find(s => s.id == id).type)
        let deletedShelfTypes = []
        await Promise.all(matchingShelfTypesById.map(async shelfType => {
          const res = await deleteShelfType(shelfType.id)
          if (res.status == 200) {
            deletedShelfTypes.push(shelfType)
          } else {
            handleAlert({
              type: 'error',
              text: `"${shelfType.type} - ${shelfType.size_class.name}" is in use and cannot be deleted.`,
              autoClose: false
            })
          }
        }))

        // display and alert for the successfully deleted shelfTypes
        if (deletedShelfTypes.length == matchingShelfTypesById.length) {
          handleAlert({
            type: 'success',
            text: `"${deletedShelfTypes[0].type}" has been successfully deleted.`,
            autoClose: true
          })
        } else if (deletedShelfTypes.length > 0) {
          handleAlert({
            type: 'success',
            text: `${deletedShelfTypes.length} size classes have been successfully deleted from "${deletedShelfTypes[0].type}".`,
            autoClose: true
          })
        }
        break
      }
      default:
        break
    }

    handleAlert({
      type: 'success',
      text: `Successfully Deleted The ${renderTableTitle.value}.`,
      autoClose: true
    })

    // update listDataTotal for pagination
    listDataTotal.value = listDataTotal.value == 0 ? 0 : listDataTotal.value - 1
  } catch (error) {
    handleAlert({
      type: 'error',
      text: error,
      autoClose: true
    })
  } finally {
    appActionIsLoadingData.value = false
    confirmationModal.value.hideModal()
  }
}
</script>

<style lang="scss" scoped>
</style>
