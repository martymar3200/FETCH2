const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    // We build our routes based on the containing layout component so all pages that will live under MainLayout need to be child paths
    // Keep routes sorted alphabetically by path
    children: [
      {
        name: 'home',
        path: '',
        component: () => import('@/pages/IndexPage.vue')
      },
      {
        name: 'accession',
        path: 'accession/:jobId?', // child path reads as "parent path + / + child_path" ex: /accession
        component: () => import('@/pages/AccessionPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_accession'
        }
      },
      {
        name: 'accession-container',
        path: 'accession/:jobId?/scan-items/:containerId?',
        component: () => import('@/pages/AccessionPage.vue'),
        beforeEnter ({ params }) {
          if (!params.containerId) {
            return {
              name: 'accession',
              params: {
                jobId: params.jobId
              }
            }
          }
        },
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_accession'
        }
      },
      {
        name: 'admin-home',
        path: 'admin',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_admin'
        }
      },
      {
        name: 'admin-groups',
        path: 'admin/groups/:groupId?',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_groups_and_permissions'
        }
      },
      {
        name: 'admin-location-manage-buildings',
        path: 'admin/manage/buildings',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-location-manage-modules',
        path: 'admin/manage/:buildingId/modules',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-location-manage-aisles',
        path: 'admin/manage/:buildingId/:moduleId/aisles',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-location-manage-ladders',
        path: 'admin/manage/:buildingId/:moduleId/:aisleId/:sideId/ladders',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-manage-owner',
        path: 'admin/manage/owner',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_owners'
        }
      },
      {
        name: 'admin-location-manage-shelves',
        path: 'admin/manage/:buildingId/:moduleId/:aisleId/:sideId/:ladderId/shelves',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_locations'
        }
      },
      {
        name: 'admin-manage-media-type',
        path: 'admin/manage/media-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_media_type'
        }
      },
      {
        name: 'admin-manage-size-class',
        path: 'admin/manage/size-class',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_size_class'
        }
      },
      {
        name: 'admin-manage-shelf-type',
        path: 'admin/manage/shelf-type',
        component: () => import('@/pages/AdminPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_manage_shelf_type'
        }
      },
      {
        name: 'record-management-items',
        path: 'record-management/items/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_item_detail'
        }
      },
      {
        name: 'record-management-shelf',
        path: 'record-management/shelf/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_shelf_detail'
        }
      },
      {
        name: 'record-management-tray',
        path: 'record-management/tray/:barcode?',
        component: () => import('@/pages/RecordManagementPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_tray_detail'
        }
      },
      {
        name: 'picklist',
        path: 'picklist/:jobId?',
        component: () => import('@/pages/PicklistPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_picklist'
        }
      },
      {
        name: 'refile',
        path: 'refile/:jobId?',
        component: () => import('@/pages/RefilePage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_refile'
        }
      },
      {
        name: 'reports',
        path: 'reports/:reportType?',
        component: () => import('@/pages/ReportsPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_reports'
        }
      },
      {
        name: 'request',
        path: 'request',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'request-details',
        path: 'request/details/:jobId',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'request-batch',
        path: 'request/batch/:jobId',
        component: () => import('@/pages/RequestPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_request'
        }
      },
      {
        name: 'search-results',
        path: 'search-results/:searchType?',
        component: () => import('@/pages/SearchPage.vue')
      },
      {
        name: 'shelving',
        path: 'shelving/:jobId?',
        component: () => import('@/pages/ShelvingPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_shelving'
        }
      },
      {
        name: 'shelving-dts',
        path: 'shelving/direct-to-shelve/:jobId?',
        component: () => import('@/pages/ShelvingPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_shelving'
        }
      },
      {
        name: 'shelving-move',
        path: 'shelving/move/:type',
        component: () => import('@/pages/ShelvingPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_move_trays_and_items_shelving_locations'
        }
      },
      {
        name: 'test',
        path: '/test',
        component: () => import('@/pages/TestPage.vue')
      },
      {
        name: 'verification',
        path: 'verification/:jobId?',
        component: () => import('@/pages/VerificationPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_verification'
        }
      },
      {
        name: 'verification-container',
        path: 'verification/:jobId?/scan-items/:containerId?',
        component: () => import('@/pages/VerificationPage.vue'),
        beforeEnter ({ params }) {
          if (!params.containerId) {
            return {
              name: 'verification',
              params: {
                jobId: params.jobId
              }
            }
          }
        },
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_verification'
        }
      },
      {
        name: 'withdrawal',
        path: 'withdrawal/:jobId?',
        component: () => import('@/pages/WithdrawalPage.vue'),
        meta: {
          requiresAuth: true,
          requiresPerm: 'can_access_withdraw'
        }
      }
    ]
  },
  // Always leave this as last one,
  // 404 page for unknown links
  {
    path: '/:catchAll(.*)*',
    component: () => import('@/pages/ErrorNotFound.vue')
  }
]

export default routes
