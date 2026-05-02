<template>
  <q-select
    v-if="!initLoading"
    ref="selectInputComponent"
    :dense="currentScreenSize == 'xs'"
    outlined
    :clearable="clearable"
    :model-value="modelValue"
    @update:model-value="updateModelValue"
    :options="localOptions"
    :option-value="optionValue"
    :option-label="optionLabel"
    :multiple="multiple"
    :use-chips="useChips"
    emit-value
    map-options
    use-input
    :hide-selected="hideSelected"
    fill-input
    input-debounce="500"
    @filter="filterOptions"
    @blur="selectInputFilterValue = ''; optionTypeTotal = 0"
    class="custom-select full-width"
    :display-value="multiple ? renderMultiSelectDisplayValues() : undefined"
    :placeholder="placeholder"
    :disable="disabled"
    :loading="loading || localLoading"
    @virtual-scroll="loadMoreOptions"
  >
    <template
      v-if="!loading && !localLoading"
      #no-option
    >
      <slot name="no-option">
        <q-item>
          <q-item-section>
            No Results
          </q-item-section>
        </q-item>
      </slot>
    </template>

    <template #option="{ itemProps, opt, selected, toggleOption }">
      <slot
        name="option"
        :item-props="itemProps"
        :opt="opt"
        :selected="selected"
        :toggle-option="toggleOption"
      >
        <q-item v-bind="itemProps">
          <q-item-section>
            <span>{{ renderLabel(opt) }}</span>
          </q-item-section>
        </q-item>
      </slot>
    </template>

    <!-- do not expose this slot, it is only use to render our custom chips and conflicts with our other select props -->
    <template
      v-if="useChips"
      #selected-item="scope"
    >
      <q-chip
        removable
        dense
        @remove="scope.removeAtIndex(scope.index)"
        :tabindex="scope.tabindex"
        class=""
      >
        {{ renderLabel(scope.opt) }}
      </q-chip>
    </template>
  </q-select>
</template>

<script setup>
import { ref, watch, inject, computed, onBeforeMount } from 'vue'
import { useOptionStore } from 'src/stores/option-store'
import { useCurrentScreenSize } from '@/composables/useCurrentScreenSize.js'

// Props
const mainProps = defineProps({
  modelValue: undefined,
  options: {
    type: Array,
    default () {
      return []
    },
    required: true
  },
  optionType: {
    type: String,
    default: ''
  },
  optionQuery: {
    type: Object,
    default () {
      return {
        size: 100
      }
    }
  },
  optionValue: {
    type: String,
    default: ''
  },
  optionLabel: {
    type: null,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  multiple: {
    type: Boolean,
    default: false
  },
  useChips: {
    type: Boolean,
    default: false
  },
  hideSelected: {
    type: Boolean,
    default: true
  },
  clearable: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  forceOptionTypeReload: {
    // this allows up for force the select input to reload is option data from the api, ex: shelving job creation, verification select uses this to ensure we get the latest list of verificaiton jobs on click
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// Compasables
const { currentScreenSize } = useCurrentScreenSize()

// Store Data
const {
  getOptions,
  getExactOption,
  getExactOptionById
} = useOptionStore()

// Local Data
const initLoading = ref(false)
const selectInputComponent = ref(null)
const selectInputFilterValue = ref('')
const localOptions = ref(mainProps.options)
const localLoading = ref(false)
const optionTypeTotal = ref(0)
const lastOptionsPage = computed(() => {
  // divide total local options by apiPageSizeDefault to get our last page value
  return Math.ceil(optionTypeTotal.value / 50)
})
const nextOptionsPage = ref(1)
const exactOption = ref(null)
const exactOptionSearchInput = ref('')

// Logic
const getNestedKeyPath = inject('get-nested-key-path')

onBeforeMount( async () => {
  initLoading.value = true

  // if the select component renders with a prepopulated modelValue we need to make sure it exists in the passed in options to properly render
  if (mainProps.optionType && Array.isArray(mainProps.modelValue) && mainProps.modelValue.length > 0 && mainProps.modelValue.every(val => mainProps.options.some(opt => opt[mainProps.optionValue] == val)) == false) {
    // if we cant find the the defined option in our passed in options list, check if we can get it directly from the api
    await Promise.all(mainProps.modelValue.map(val => {
      // gets every exact option if our modelValue is prepopulated via an array of values and data is not found in the current loaded options
      return getExactOptionById(mainProps.optionType, val, true)
    }))
  } else if (mainProps.optionType && mainProps.modelValue !== null && !Array.isArray(mainProps.modelValue) && mainProps.options.some(opt => opt[mainProps.optionValue] == mainProps.modelValue) == false) {
    // gets a single exact option if our modelValue is prepopulated and data is not found in the current loaded options
    await getExactOptionById(mainProps.optionType, mainProps.modelValue)
  }
  initLoading.value = false
})

watch(() => mainProps.options, (updatedOptions) => {
  localOptions.value = updatedOptions
  if (selectInputFilterValue.value) {
    // we need to force filtering to re render to catch our updated options from the api
    selectInputComponent.value.filter()
  }
})

const updateModelValue = (value) => {
  emit('update:modelValue', value)
}
const filterOptions = async (val, update) => {
  // if there is an optionType then we need to get a the intial list of options from the api based on the optionType passed in
  // the passed in optionType should match an http endpoint
  if (mainProps.forceOptionTypeReload || mainProps.optionType !== '' && localOptions.value.length <= 1 && optionTypeTotal.value == 0) {
    // for string based option labels we can set these as our default sort by, nested option labels must be passed in via optionQuery
    const defaultSortBy = mainProps.optionLabel.toString().includes('.') ? null : mainProps.optionLabel
    const res = await getOptions(mainProps.optionType, {
      sort_by: defaultSortBy,
      ...mainProps.optionQuery
    }, true)
    optionTypeTotal.value = res.data.total
  }

  update(async () => {
    // set the user inputed filter value in state
    selectInputFilterValue.value = selectInputComponent.value.$el.querySelector('input').value

    if (mainProps.optionLabel.toString().includes('.')) {
      // if we pass in a arrow function label we convert it to read as a property key
      // ex opt => opt.barcode.value we only need 'barcode.value' from that function
      const paramPath = mainProps.optionLabel.toString().split('.').slice(1).join('.')
      localOptions.value = mainProps.options.filter(opt => {
        return getNestedKeyPath(opt, paramPath).toString().toLowerCase().indexOf(selectInputFilterValue.value.toLowerCase()) > -1
      })
    } else {
      // filter all passed in options based on user input value
      if (mainProps.optionLabel !== '') {
        localOptions.value = mainProps.options.filter(opt => opt[mainProps.optionLabel]?.toString().toLowerCase().indexOf(selectInputFilterValue.value.toLowerCase()) > -1)
      } else {
        localOptions.value = mainProps.options.filter(opt => opt.toString().toLowerCase().indexOf(selectInputFilterValue.value.toLowerCase()) > -1)
      }
    }

    // if there is a previous exact option search
    // clear out our stored exactOption if our current select input filter doesnt match the exact search input
    if (exactOptionSearchInput.value !== '' && exactOptionSearchInput.value !== selectInputFilterValue.value) {
      exactOption.value = null
    }

    // if we are populating our select via an api list that has multiple pages and the users filter result returns nothing
    // we need to query search through the entire api list option to make sure there truly is no matching result across all pages
    // this only runs if our filters above find no results within the current paged data set and there is no previous exactOption resulting in an empty array
    if (mainProps.optionType !== '' && localOptions.value.length == 0 && lastOptionsPage.value !== 1 && !(Array.isArray(exactOption.value) && exactOption.value.length == 0)) {
      localLoading.value = true
      exactOptionSearchInput.value = selectInputFilterValue.value
      const res = await getExactOption(mainProps.optionType, {
        ...mainProps.optionQuery,
        search: selectInputFilterValue.value
      })
      exactOption.value = res
      localLoading.value = false
    }
  })
}
const loadMoreOptions = async ({ to, ref }) => {
  const lastIndex = localOptions.value.length - 1
  // only load more options if were at the bottom of the list, not on the last page and not trying to filter search
  if (mainProps.optionType !== '' && !localLoading.value && to === lastIndex && nextOptionsPage.value < lastOptionsPage.value && selectInputFilterValue.value == '') {
    localLoading.value = true
    const defaultSortBy = mainProps.optionLabel.toString().includes('.') ? null : mainProps.optionLabel
    const res = await getOptions(mainProps.optionType, {
      sort_by: defaultSortBy,
      ...mainProps.optionQuery,
      page: nextOptionsPage.value + 1
    }, true)
    optionTypeTotal.value = res.data.total

    nextOptionsPage.value++
    // calls and internal qSelect function that handles refreshing the list with the updating options at the last index position
    ref.refresh()
    localLoading.value = false
  }
}
const renderLabel = (opt) => {
  if (mainProps.optionLabel.toString().includes('.')) {
    // if we pass in a arrow function label we convert it to read as a property key
    // ex opt => opt.barcode.value we only need 'barcode.value' from that function
    const paramPath = mainProps.optionLabel.toString().split('.').slice(1).join('.')
    return getNestedKeyPath(opt, paramPath)
  } else if (mainProps.optionLabel) {
    return opt[mainProps.optionLabel]
  } else {
    return opt
  }
}
const renderMultiSelectDisplayValues = () => {
  if (mainProps.modelValue && mainProps.modelValue.length >= 5) {
    // if user selects more than 4 values we change the select display value to say the first 4 values + more count message
    // ex (select display if user selected 9 options): A,B,C,D + 5 more
    const multiDisplayLabel = mainProps.modelValue.map(idVal => {
      return renderLabel(mainProps.options.find(opt => opt.id == idVal))
    }).splice(0, 4)

    return `${multiDisplayLabel} + ${mainProps.modelValue.length - 4} more`
  } else if (mainProps.modelValue) {
    // display the single selected value
    const displayLabel = mainProps.modelValue.map(idVal => {
      return renderLabel(mainProps.options.find(opt => opt.id == idVal))
    })
    return displayLabel.toString()
  } else {
    // this is the default value for the displayValue prop on the select input
    return undefined
  }
}
</script>

<style lang="scss" scoped>
.custom-select {
  :deep(.q-placeholder) {
    width: inherit;
  }

  :deep(.q-field__control) {
    &::before {
      border-color: $color-black;
    }

    .q-chip__icon {
      margin-top: -3px;
    }
  }

  &.q-field--disabled {
    :deep(.q-field__control) {
      &::before {
        border-color: rgba($color-black, .25);
      }
    }
  }
}
</style>
