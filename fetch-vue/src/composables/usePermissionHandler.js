import { useUserStore } from '@/stores/user-store'
import { storeToRefs } from 'pinia'

export function usePermissionHandler () {
  const { userData } = storeToRefs(useUserStore())

  function checkUserPermission (permissionString) {
    // check the user data in store and see if we have a permission that is an exact match to the passed in permissionString
    return userData.value.permissions?.some(perm => perm === permissionString)
  }

  return {
    checkUserPermission
  }
}
