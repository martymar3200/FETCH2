import { ref } from 'vue'

export function useFileSystemAccessHandler () {
  const fileReference = ref(null)
  const fileContent = ref(null)

  // allows user to select text files in a native directory
  async function selectTextFile () {
    // destructure the array response from showOpenFilePicker() and set that as the fileHandle
    [fileReference.value] = await window.showOpenFilePicker({
      types: [
        {
          description: 'Text Files',
          accept: {
            'text/plain': ['.txt']
          }
        }
      ],
      excludeAcceptAllOption: true,
      multiple: false
    })

    const selectedFile = await fileReference.value.getFile()

    fileContent.value = await selectedFile.text()
  }

  // allows users to save a new text file to the users system
  async function saveAsTextFile (textContent) {
    const fileHandle = await window.showSaveFilePicker({
      types: [
        {
          description: 'Text File',
          accept: {
            'text/plain': ['.txt']
          }
        }
      ]
    })

    const writeableFile = await fileHandle.createWritable()

    await writeableFile.write(textContent)
    await writeableFile.close()

    // save the new file reference
    fileReference.value = fileHandle
  }

  // allows users to save edits to the selected text fileReference
  async function updateTextFile (textContent) {
    const writeableFile = await fileReference.value.createWritable()

    await writeableFile.write(textContent)
    await writeableFile.close()
  }

  return {
    fileReference,
    fileContent,
    selectTextFile,
    saveAsTextFile,
    updateTextFile
  }
}
