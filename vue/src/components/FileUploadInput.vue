<template>
  <div class="file-upload">
    <div
      :data-active="activeState"
      @dragenter.prevent="setActive"
      @dragover.prevent="setActive"
      @dragleave.prevent="setInactive"
      @drop.prevent="handleDrop"
      class="file-upload-box"
      :class="activeState ? `file-upload-box-active ${inputClass}` : inputClass"
    >
      <slot
        name="content"
        :drop-zone-active="activeState"
        :handle-input="handleInput"
      >
        <label
          for="file-input"
          class="text-center"
        >
          <span class="text-h6">
            <q-icon
              :name="'mdi-upload'"
              size="26px"
              style="vertical-align: top;"
            />
            Click <a
              class="text-primary"
              tabindex="0"
            >Here</a> or Drag File to Upload
          </span>

          <input
            type="file"
            id="file-input"
            :multiple="allowMultipleFiles"
            @change="handleInput"
          />
        </label>
      </slot>
    </div>

    <ul
      v-if="files.length > 0"
      class="file-upload-list"
    >
      <li
        v-for="file in files"
        :key="file.id"
        class="text-body1"
      >
        <span>{{ file.name }}</span>
        <q-btn
          icon="cancel"
          color="negative"
          flat
          round
          dense
          @click="removeFile(file)"
        />
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, inject, watch } from 'vue'

// Props
const mainProps = defineProps({
  allowMultipleFiles: {
    type: Boolean,
    default: false
  },
  allowedFileTypes: {
    type: Array,
    default: () => {
      return []
    }
  },
  inputClass: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['file-change'])

// Local Data
const activeState = ref(false)
const files = ref([])

// Logic
const handleAlert = inject('handle-alert')

const handleDrop = (evt) => {
  evt.preventDefault()

  setInactive()
  addFiles([...evt.dataTransfer.files])
}
const handleInput = (evt) => {
  addFiles(evt.target.files)

  // reset so that selecting the same file again will still cause it to fire this change
  evt.target.value = null
}
const setActive = () => {
  activeState.value = true
}
const setInactive = () => {
  setTimeout(() => {
    activeState.value = false
  }, 50)
}


watch(files, () => {
  emit('file-change', files.value)
},
{ deep : true }
)
const addFiles = (newFiles) => {
  // map the file or files and create a file object
  const uploadableFiles = [...newFiles].map(newFile => {
    return {
      file: newFile,
      name: newFile.name,
      id: `${newFile.name}-${newFile.size}-${newFile.lastModified}-${newFile.type}`,
      url: URL.createObjectURL(newFile)
    }
  })

  // validate the files and cancel adding if a file is invalid
  for (const f of uploadableFiles) {
    if (!validateFile(f)) {
      handleAlert({
        type: 'error',
        text: 'One or More Files Selected Are Not Allowed!',
        autoClose: true
      })
      return
    }
  }

  if (mainProps.allowMultipleFiles) {
    // set the files and filter out any files that already exist in local data
    files.value = files.value.concat(uploadableFiles.filter(file => !files.value.some(({ id }) => id === file.id)))
  } else {
    files.value = uploadableFiles
  }
}
const removeFile = (file) => {
  // find the files with the matching id and filter it out
  const index = files.value.indexOf(file)

  if (index > -1) {
    files.value.splice(index, 1)
  }
}
const validateFile = (file) => {
  if (mainProps.allowedFileTypes.length == 0) {
    // allow all file types
    return true
  } else {
    // check that the passed in file is an allowed type extension
    const fileType = file.name.split('.').pop()
    return mainProps.allowedFileTypes.some(type => `.${fileType}`.includes(type))
  }
}

defineExpose({ files })
</script>

<style lang="scss" scoped>
.file-upload {
  position: relative;

  &-box {
    position: relative;
    border: 2px dashed $color-black;
    border-radius: 4px;
    transition: all .3s ease;

    &:hover, &-active {
      border-color: $primary;
      background-color: rgba($accent, 0.15);
    }

    label {
      cursor: pointer;
      display: block;

      input[type=file]:not(:focus-visible) {
        // hides the input from ui
        position: absolute !important;
        width: 1px !important;
        height: 1px !important;
        padding: 0 !important;
        margin: -1px !important;
        overflow: hidden !important;
        clip: rect(0, 0, 0, 0) !important;
        white-space: nowrap !important;
        border: 0 !important;
      }
    }
  }

  &-list {
    list-style: none;

    li {
      position: relative;
      margin-left: 0;
      padding-left: 0;
    }
  }
}
</style>